#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

BUILD_GRADLE="$PROJECT_ROOT/build.gradle.kts"

# Get current version from build.gradle.kts
get_current_version() {
    grep -E '^version\s*=\s*"[0-9]+\.[0-9]+\.[0-9]+"' "$BUILD_GRADLE" | sed -E 's/.*"([0-9]+\.[0-9]+\.[0-9]+)".*/\1/'
}

# Bump version based on type (major, minor, patch)
bump_version() {
    local current=$1
    local type=$2

    IFS='.' read -r major minor patch <<< "$current"

    case $type in
        major)
            major=$((major + 1))
            minor=0
            patch=0
            ;;
        minor)
            minor=$((minor + 1))
            patch=0
            ;;
        patch)
            patch=$((patch + 1))
            ;;
        *)
            echo -e "${RED}Invalid bump type: $type${NC}"
            exit 1
            ;;
    esac

    echo "$major.$minor.$patch"
}

# Update version in build.gradle.kts
update_version() {
    local new_version=$1
    sed -i '' "s/^version = \"[0-9]*\.[0-9]*\.[0-9]*\"/version = \"$new_version\"/" "$BUILD_GRADLE"
}

# Print usage
usage() {
    echo "Usage: $0 [patch|minor|major|<version>]"
    echo ""
    echo "Examples:"
    echo "  $0 patch       # 0.5.0 -> 0.5.1"
    echo "  $0 minor       # 0.5.0 -> 0.6.0"
    echo "  $0 major       # 0.5.0 -> 1.0.0"
    echo "  $0 1.2.3       # Set specific version"
    echo ""
    echo "Options:"
    echo "  --skip-build   Skip the Android build verification"
    echo "  --dry-run      Show what would be done without making changes"
    exit 1
}

# Parse arguments
SKIP_BUILD=false
DRY_RUN=false
VERSION_ARG=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        -h|--help)
            usage
            ;;
        *)
            VERSION_ARG=$1
            shift
            ;;
    esac
done

if [ -z "$VERSION_ARG" ]; then
    usage
fi

# Get current version
CURRENT_VERSION=$(get_current_version)
echo -e "${YELLOW}Current version: $CURRENT_VERSION${NC}"

# Determine new version
if [[ "$VERSION_ARG" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    NEW_VERSION="$VERSION_ARG"
elif [[ "$VERSION_ARG" =~ ^(patch|minor|major)$ ]]; then
    NEW_VERSION=$(bump_version "$CURRENT_VERSION" "$VERSION_ARG")
else
    echo -e "${RED}Invalid version argument: $VERSION_ARG${NC}"
    usage
fi

echo -e "${GREEN}New version: $NEW_VERSION${NC}"

if [ "$DRY_RUN" = true ]; then
    echo -e "${YELLOW}[DRY RUN] Would perform the following:${NC}"
    echo "  1. Build Android release"
    echo "  2. Update version in build.gradle.kts to $NEW_VERSION"
    echo "  3. Commit: 'Bump version to $NEW_VERSION'"
    echo "  4. Create tag: $NEW_VERSION"
    echo "  5. Push commit and tag to origin"
    exit 0
fi

# Check for uncommitted changes
if [ -n "$(git status --porcelain)" ]; then
    echo -e "${RED}Error: You have uncommitted changes. Please commit or stash them first.${NC}"
    git status --short
    exit 1
fi

# Step 1: Build Android to verify compilation
if [ "$SKIP_BUILD" = false ]; then
    echo -e "${YELLOW}Building Android release to verify compilation...${NC}"
    ./gradlew assembleRelease --no-daemon
    echo -e "${GREEN}Build successful!${NC}"
else
    echo -e "${YELLOW}Skipping build verification${NC}"
fi

# Step 2: Update version
echo -e "${YELLOW}Updating version to $NEW_VERSION...${NC}"
update_version "$NEW_VERSION"

# Step 3: Commit
echo -e "${YELLOW}Committing version bump...${NC}"
git add "$BUILD_GRADLE"
git commit -m "Bump version to $NEW_VERSION"

# Step 4: Create tag
echo -e "${YELLOW}Creating tag $NEW_VERSION...${NC}"
git tag "$NEW_VERSION"

# Step 5: Push
echo -e "${YELLOW}Pushing to origin...${NC}"
git push origin main
git push origin "$NEW_VERSION"

echo ""
echo -e "${GREEN}Release $NEW_VERSION complete!${NC}"
echo -e "${GREEN}JitPack will build automatically at: https://jitpack.io/#xputnikx/rateme/${NEW_VERSION}${NC}"
