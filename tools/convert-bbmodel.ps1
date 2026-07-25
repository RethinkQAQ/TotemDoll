param(
    [Parameter(Mandatory = $true)][string]$GeometryFile,
    [Parameter(Mandatory = $true)][string]$AnimationFile,
    [Parameter(Mandatory = $true)][string]$OutputDirectory,
    [string]$Texture = "totemdoll:textures/item/doll/alex.png"
)

$ErrorActionPreference = "Stop"
$geometry = Get-Content -LiteralPath $GeometryFile -Raw | ConvertFrom-Json
$project = Get-Content -LiteralPath $AnimationFile -Raw | ConvertFrom-Json

function Vector3($value, $fallback = @(0, 0, 0)) {
    if ($null -eq $value -or @($value).Count -lt 3) { return $fallback }
    return ,@([float]$value[0], [float]$value[1], [float]$value[2])
}

$groupOrigins = @{}
foreach ($group in @($project.groups)) {
    if ($group.name) { $groupOrigins[$group.name] = Vector3 $group.origin }
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
$rootGroup = @($geometry.groups)[0]
$geometryOutput = [ordered]@{
    format = 1
    texture_width = 64
    texture_height = 64
    display = $geometry.display
    bones = @((ConvertBone $rootGroup))
}
$animationOutput = [ordered]@{ format = 1; animations = $convertedAnimations }
$geometryOutput | ConvertTo-Json -Depth 32 | Set-Content -LiteralPath (Join-Path $targetModels "geometry.json") -Encoding UTF8
$animationOutput | ConvertTo-Json -Depth 32 | Set-Content -LiteralPath (Join-Path $targetModels "animations.json") -Encoding UTF8

$styleFile = Join-Path $OutputDirectory "style.json"
$style = Get-Content -LiteralPath $styleFile -Raw | ConvertFrom-Json
$style.enabled = $true
$style.textures.base = $Texture
$style.animations = [ordered]@{
    idle_head_shake = [ordered]@{ animation = "idle_head_shake"; trigger = "loop"; priority = 20 }
    screen_wave = [ordered]@{ animation = "screen_wave"; trigger = "on_screen_open"; priority = 60 }
    totem_struggle = [ordered]@{ animation = $struggleAnimation; trigger = "on_totem_activate"; priority = 100 }
}
$style | ConvertTo-Json -Depth 32 | Set-Content -LiteralPath $styleFile -Encoding UTF8
