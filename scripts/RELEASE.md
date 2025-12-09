# Release Process

This document describes how to release a new version of the RateMe library.

## Prerequisites

- Clean working directory (no uncommitted changes)
- Push access to the repository

## Release Script

The `release.sh` script automates the release process:

```bash
./scripts/release.sh <version>
```

### Version Bump Types

```bash
# Bump patch version (0.5.0 -> 0.5.1)
./scripts/release.sh patch

# Bump minor version (0.5.0 -> 0.6.0)
./scripts/release.sh minor

# Bump major version (0.5.0 -> 1.0.0)
./scripts/release.sh major

# Set specific version
./scripts/release.sh 1.0.0
```

### Options

| Option | Description |
|--------|-------------|
| `--skip-build` | Skip Android build verification (faster) |
| `--dry-run` | Preview changes without executing |
| `-h, --help` | Show usage information |

### Examples

```bash
# Standard patch release
./scripts/release.sh patch

# Quick release without build verification
./scripts/release.sh patch --skip-build

# Preview what would happen
./scripts/release.sh minor --dry-run
```

## What the Script Does

1. **Verifies** no uncommitted changes exist
2. **Builds** Android release to verify compilation
3. **Updates** version in `build.gradle.kts`
4. **Commits** the version change
5. **Tags** the commit with the version number
6. **Pushes** commit and tag to origin

## After Release

JitPack automatically builds the new version when the tag is pushed.

Check build status at: https://jitpack.io/#xputnikx/rateme

## Manual Release

If you prefer to release manually:

```bash
# 1. Update version in build.gradle.kts
# version = "X.Y.Z"

# 2. Build to verify
./gradlew assembleRelease

# 3. Commit
git add build.gradle.kts
git commit -m "Bump version to X.Y.Z"

# 4. Tag and push
git tag X.Y.Z
git push origin main
git push origin X.Y.Z
```
