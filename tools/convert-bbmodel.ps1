param(
    [Parameter(Mandatory = $true)][string]$InputFile,
    [Parameter(Mandatory = $true)][string]$OutputDirectory,
    [string]$StyleId = "example:my_doll",
    [string]$StyleName = "My Totem Doll",
    [string]$Texture = "textures/base.png"
)

$ErrorActionPreference = "Stop"
$project = Get-Content -LiteralPath $InputFile -Raw | ConvertFrom-Json
$geometry = $project.geometry
if ($null -eq $geometry) { $geometry = $project }
if ($project.parent -or $project.overrides) {
    throw "Minecraft parent/overrides are not supported by format:3 mesh export"
}

function Vector3($value, $fallback = @(0, 0, 0)) {
    if ($null -eq $value -or @($value).Count -lt 3) { return $fallback }
    return ,@([float]$value[0], [float]$value[1], [float]$value[2])
}

$groupOrigins = @{}
foreach ($group in @($project.groups)) {
    if ($group.name) {
        # Blockbench Java models use a centered coordinate system for pivots
        # (X/Z are commonly -8..8), while the Minecraft item geometry uses
        # 0..16 coordinates. Convert animation pivots into model space.
        $origin = @($group.origin)
        $pivotX = [System.Convert]::ToSingle($origin[0]) + 8.0
        $pivotY = [System.Convert]::ToSingle($origin[1])
        $pivotZ = [System.Convert]::ToSingle($origin[2]) + 8.0
        $groupOrigins[$group.name] = [float[]]($pivotX, $pivotY, $pivotZ)
    }
}

function ConvertCube($element) {
    $from = @(Vector3 $element.from)[0]
    $to = @(Vector3 $element.to)[0]
    $uv = @(0, 0)
    if ($element.faces.north -and $element.faces.north.uv) {
        $uv = @([int][float]$element.faces.north.uv[0], [int][float]$element.faces.north.uv[1])
    }
    $faces = [ordered]@{}
    foreach ($direction in @("down", "up", "north", "south", "west", "east")) {
        $face = $element.faces.$direction
        if ($null -eq $face -or $null -eq $face.uv -or @($face.uv).Count -lt 4) { continue }
        $faces[$direction] = [ordered]@{
            uv = @([float]$face.uv[0], [float]$face.uv[1], [float]$face.uv[2], [float]$face.uv[3])
            rotation = if ($null -ne $face.rotation) { [int]$face.rotation } else { 0 }
        }
    }
    [ordered]@{
        origin = $from
        size = @(([float]$to[0] - [float]$from[0]), ([float]$to[1] - [float]$from[1]), ([float]$to[2] - [float]$from[2]))
        uv = $uv
        faces = $faces
    }
}

function ElementRotation($element) {
    $rotation = $element.rotation
    if ($null -eq $rotation) { return ,@(0.0, 0.0, 0.0) }
    if ($null -ne $rotation.angle -and $rotation.axis) {
        $angle = [float]$rotation.angle
        switch ([string]$rotation.axis) {
            "x" { return ,@($angle, 0.0, 0.0) }
            "y" { return ,@(0.0, $angle, 0.0) }
            "z" { return ,@(0.0, 0.0, $angle) }
        }
    }
    return ,@([float]$rotation.x, [float]$rotation.y, [float]$rotation.z)
}

function HasRotation($rotation) {
    return [math]::Abs([float]$rotation[0]) -gt 0.0001 -or
           [math]::Abs([float]$rotation[1]) -gt 0.0001 -or
           [math]::Abs([float]$rotation[2]) -gt 0.0001
}

function ConvertBone($group) {
    $pivot = if ($groupOrigins.ContainsKey($group.name)) { $groupOrigins[$group.name] } else { @(Vector3 $group.origin)[0] }
    $cubes = @()
    $children = @()
    $cubeIndex = 0
    foreach ($child in @($group.children)) {
        if ($child -is [int] -or $child -is [long] -or $child -is [double]) {
            $element = $geometry.elements[[int]$child]
            $rotation = @(ElementRotation $element)[0]
            if (HasRotation $rotation) {
                # ModelPart rotates an entire part, not an individual cube. Preserve
                # Blockbench element rotations by creating a private child bone.
                $elementPivot = if ($element.rotation.origin) { @(Vector3 $element.rotation.origin)[0] } else { $pivot }
                $children += [ordered]@{
                    name = "{0}__cube_{1}" -f $group.name, $cubeIndex
                    pivot = $elementPivot
                    rotation = $rotation
                    cubes = @((ConvertCube $element))
                    children = @()
                }
            } else {
                $cubes += ConvertCube $element
            }
            $cubeIndex++
        } elseif ($child.name) {
            $children += ConvertBone $child
        }
    }
    [ordered]@{
        name = [string]$group.name
        pivot = $pivot
        # The .bbmodel group rotations are animation pose values. They are not
        # copied here, otherwise the keyframes would be applied a second time.
        rotation = @(0.0, 0.0, 0.0)
        cubes = $cubes
        children = $children
    }
}

# Rebuild the geometry hierarchy from Blockbench's outliner. The exported
# item model often flattens groups, while the .bbmodel outliner preserves the
# actual parent/child relationships used by animations.
$geometryGroupsByName = @{}
function IndexGeometryGroups($group) {
    if ($group.name) { $geometryGroupsByName[[string]$group.name] = $group }
    foreach ($child in @($group.children)) {
        if ($child -isnot [int] -and $child -isnot [long] -and $child -isnot [double] -and $child.name) {
            IndexGeometryGroups $child
        }
    }
}

$projectGroupsByUuid = @{}
foreach ($group in @($project.groups)) {
    if ($group.uuid) { $projectGroupsByUuid[[string]$group.uuid] = $group }
}

$projectOutlinerByUuid = @{}
function IndexOutlinerNodes($node) {
    if ($node -isnot [string] -and $node.uuid) {
        $projectOutlinerByUuid[[string]$node.uuid] = $node
        foreach ($child in @($node.children)) { IndexOutlinerNodes $child }
    }
}
if ($project.outliner) { IndexOutlinerNodes $project.outliner }

function OutlinerGroupName($node) {
    $uuid = if ($node -is [string]) { $node } elseif ($node.uuid) { [string]$node.uuid } else { $null }
    if ($uuid -and $projectGroupsByUuid.ContainsKey($uuid)) {
        return [string]$projectGroupsByUuid[$uuid].name
    }
    return $null
}

function ConvertBoneHierarchy($name, $visited = @{}) {
    if (-not $geometryGroupsByName.ContainsKey($name)) { return $null }
    if ($visited.ContainsKey($name)) { throw "Circular bone hierarchy at $name" }
    $nextVisited = @{} + $visited
    $nextVisited[$name] = $true
    $group = $geometryGroupsByName[$name]
    $pivot = if ($groupOrigins.ContainsKey($name)) { $groupOrigins[$name] } else { @(Vector3 $group.origin)[0] }
    $cubes = @()
    $rotatedChildren = @()
    $cubeIndex = 0
    foreach ($child in @($group.children)) {
        if ($child -is [int] -or $child -is [long] -or $child -is [double]) {
            $element = $geometry.elements[[int]$child]
            $rotation = @(ElementRotation $element)[0]
            if (HasRotation $rotation) {
                $elementPivot = if ($element.rotation.origin) { @(Vector3 $element.rotation.origin)[0] } else { $pivot }
                $rotatedChildren += [ordered]@{
                    name = "{0}__cube_{1}" -f $name, $cubeIndex
                    pivot = $elementPivot
                    rotation = $rotation
                    cubes = @((ConvertCube $element))
                    children = @()
                }
            } else {
                $cubes += ConvertCube $element
            }
            $cubeIndex++
        }
    }
    $children = @($rotatedChildren)
    $groupUuid = ($projectGroupsByUuid.Values | Where-Object { $_.name -eq $name } | Select-Object -First 1).uuid
    $outliner = if ($groupUuid -and $projectOutlinerByUuid.ContainsKey([string]$groupUuid)) {
        $projectOutlinerByUuid[[string]$groupUuid]
    } else { $null }
    foreach ($childNode in @($outliner.children)) {
        $childName = OutlinerGroupName $childNode
        if ($childName) {
            $childBone = ConvertBoneHierarchy $childName $nextVisited
            if ($null -ne $childBone) { $children += $childBone }
        }
    }
    [ordered]@{
        name = $name
        pivot = $pivot
        rotation = @(0.0, 0.0, 0.0)
        cubes = $cubes
        children = $children
    }
}

IndexGeometryGroups (@($geometry.groups)[0])

function ConvertFrames($animator, $channel) {
    $frames = @()
    foreach ($keyframe in @($animator.keyframes)) {
        if ($keyframe.channel -ne $channel) { continue }
        $point = @($keyframe.data_points)[0]
        $value = @(Vector3 @([float]$point.x, [float]$point.y, [float]$point.z))[0]
        $frames += [ordered]@{
            time = [float]$keyframe.time * 20
            value = $value
            interpolation = if ($keyframe.interpolation) { [string]$keyframe.interpolation } else { "linear" }
        }
    }
    return $frames
}

$convertedAnimations = [ordered]@{}
foreach ($animation in @($project.animations)) {
    $bones = [ordered]@{}
    foreach ($property in $animation.animators.PSObject.Properties) {
        $animator = $property.Value
        if (-not $animator.name -or -not $animator.keyframes) { continue }
        $timeline = [ordered]@{}
        $rotation = ConvertFrames $animator "rotation"
        $position = ConvertFrames $animator "position"
        $scale = ConvertFrames $animator "scale"
        if ($rotation.Count -gt 0) { $timeline.rotation = $rotation }
        if ($position.Count -gt 0) { $timeline.position = $position }
        if ($scale.Count -gt 0) { $timeline.scale = $scale }
        if ($timeline.Count -gt 0) { $bones[$animator.name] = $timeline }
    }
    $convertedAnimations[$animation.name] = [ordered]@{
        loop = ([string]$animation.loop -eq "loop")
        length = [int]([float]$animation.length * 20)
        bones = $bones
    }
}

$struggleAnimation = $convertedAnimations.Keys | Where-Object { $_ -ne "idle_head_shake" -and $_ -ne "screen_wave" } | Select-Object -First 1
if ($struggleAnimation) {
    # Totem activation is an event action and must return to idle after one play.
    $convertedAnimations[$struggleAnimation].loop = $false
}

$targetModels = Join-Path $OutputDirectory "models"
New-Item -ItemType Directory -Force -Path $targetModels | Out-Null
$textureOutput = Join-Path $OutputDirectory $Texture
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $textureOutput) | Out-Null

function CopyTexture($bbmodel, $inputFile, $destination) {
    $textureEntry = @($bbmodel.textures)[0]
    if ($null -eq $textureEntry) {
        throw "The .bbmodel does not contain a texture. Add a PNG texture before exporting format:3."
    }

    $source = [string]$textureEntry.source
    if ($source -match '^data:image/png;base64,(.+)$') {
        [IO.File]::WriteAllBytes($destination, [Convert]::FromBase64String($Matches[1]))
        return
    }

    $texturePath = [string]$textureEntry.path
    if ([string]::IsNullOrWhiteSpace($texturePath)) { $texturePath = [string]$textureEntry.name }
    if ([string]::IsNullOrWhiteSpace($texturePath)) {
        throw "The .bbmodel texture has no source or path."
    }
    $sourcePath = Join-Path (Split-Path -Parent (Resolve-Path $inputFile)) $texturePath
    if (-not (Test-Path -LiteralPath $sourcePath -PathType Leaf)) {
        throw "Texture file was not found: $sourcePath"
    }
    if (-not ([IO.Path]::GetExtension($sourcePath) -ieq ".png")) {
        throw "format:3 textures must be PNG files: $sourcePath"
    }
    Copy-Item -LiteralPath $sourcePath -Destination $destination -Force
}
$rootGroup = @($geometry.groups)[0]
$rootName = [string]$rootGroup.name
$rootBone = $null
if ($project.outliner) {
    $rootNameFromOutliner = OutlinerGroupName $project.outliner
    if ($rootNameFromOutliner) { $rootName = $rootNameFromOutliner }
    $rootBone = ConvertBoneHierarchy $rootName
}
if ($null -eq $rootBone) { $rootBone = ConvertBone $rootGroup }
$geometryOutput = [ordered]@{
    format = 1
    texture_width = 64
    texture_height = 64
    display = $geometry.display
    bones = @($rootBone)
}
$animationOutput = [ordered]@{ format = 1; animations = $convertedAnimations }
$geometryOutput | ConvertTo-Json -Depth 32 | Set-Content -LiteralPath (Join-Path $targetModels "geometry.json") -Encoding UTF8
$hasAnimations = $convertedAnimations.Count -gt 0
if ($hasAnimations) {
    $animationOutput | ConvertTo-Json -Depth 32 | Set-Content -LiteralPath (Join-Path $targetModels "animations.json") -Encoding UTF8
}

$styleFile = Join-Path $OutputDirectory "style.json"
$model = [ordered]@{ type = "mesh"; geometry = "models/geometry.json" }
if ($hasAnimations) { $model.animations = "models/animations.json" }
$style = [ordered]@{
    format = 3
    id = $StyleId
    name = $StyleName
    model = $model
    textures = [ordered]@{ base = $Texture }
    features = [ordered]@{ animations = $hasAnimations; dynamic_textures = $false }
}
if ($hasAnimations) {
    $style.animations = [ordered]@{}
    foreach ($name in $convertedAnimations.Keys) {
        $trigger = if ($name -eq $struggleAnimation) { "on_totem_activate" } else { "loop" }
        $priority = if ($trigger -eq "on_totem_activate") { 100 } else { 20 }
        $style.animations[$name] = [ordered]@{ animation = $name; trigger = $trigger; priority = $priority }
    }
}
$style | ConvertTo-Json -Depth 32 | Set-Content -LiteralPath $styleFile -Encoding UTF8
CopyTexture $project $InputFile $textureOutput
