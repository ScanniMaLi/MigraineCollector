param (
    [Parameter(Mandatory=$true, HelpMessage="Version tag (e.g. v1.0)")]
    [string]$Tag
)

# Validate tag format (simple check for 'v')
if ($Tag -notmatch "^v") {
    Write-Warning "Tag should typically start with 'v' (e.g. v1.0) to match the CI workflow trigger."
    $confirm = Read-Host "Continue with tag '$Tag'? (y/n)"
    if ($confirm -ne 'y') { exit }
}

Write-Host "Creating git tag: $Tag"
git tag $Tag

if ($?) {
    Write-Host "Pushing tag to origin..."
    git push origin $Tag
    
    if ($?) {
        Write-Host "Success! Tag $Tag pushed to origin." -ForegroundColor Green
        Write-Host "This should trigger the GitHub Action build." -ForegroundColor Cyan
    } else {
        Write-Error "Failed to push tag to origin."
    }
} else {
    Write-Error "Failed to create tag. It might already exist."
}
