#!/bin/bash

# Script to download Figma assets for Tap to Pay UI
# This downloads the payment card logos and NFC icon from Figma's CDN
# and saves them to the Android drawable directory

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Downloading Figma Assets ===${NC}\n"

# Target directory
DRAWABLE_DIR="../app/src/main/res/drawable"
mkdir -p "$DRAWABLE_DIR"

# Asset URLs from Figma - Download each asset
echo -e "${BLUE}Downloading Visa logo...${NC}"
curl -f -L -o "$DRAWABLE_DIR/ic_payment_visa.png" "https://www.figma.com/api/mcp/asset/c04f40c8-565a-49ac-a77e-ec05627ad19f"
echo -e "${GREEN}✓ Downloaded ic_payment_visa.png${NC}"

echo -e "${BLUE}Downloading Mastercard logo...${NC}"
curl -f -L -o "$DRAWABLE_DIR/ic_payment_mastercard.png" "https://www.figma.com/api/mcp/asset/00f67878-e2ee-47e7-b420-876a576954d6"
echo -e "${GREEN}✓ Downloaded ic_payment_mastercard.png${NC}"

echo -e "${BLUE}Downloading Discover logo...${NC}"
curl -f -L -o "$DRAWABLE_DIR/ic_payment_discover.png" "https://www.figma.com/api/mcp/asset/ce571b86-56f5-4270-8974-51f79170d3b2"
echo -e "${GREEN}✓ Downloaded ic_payment_discover.png${NC}"

echo -e "${BLUE}Downloading Amex logo...${NC}"
curl -f -L -o "$DRAWABLE_DIR/ic_payment_amex.png" "https://www.figma.com/api/mcp/asset/b8fa4dd9-7fa2-43c0-a264-9fa7dba1069f"
echo -e "${GREEN}✓ Downloaded ic_payment_amex.png${NC}"

echo -e "${BLUE}Downloading JCB logo...${NC}"
curl -f -L -o "$DRAWABLE_DIR/ic_payment_jcb.png" "https://www.figma.com/api/mcp/asset/d81d73f8-7638-46d4-b523-01bf2c4d9105"
echo -e "${GREEN}✓ Downloaded ic_payment_jcb.png${NC}"

echo -e "${BLUE}Downloading NFC contactless icon...${NC}"
curl -f -L -o "$DRAWABLE_DIR/ic_nfc_contactless.png" "https://www.figma.com/api/mcp/asset/df422283-812b-46b5-b8d4-260c9c9ebd4d"
echo -e "${GREEN}✓ Downloaded ic_nfc_contactless.png${NC}"

echo -e "\n${GREEN}=== All assets downloaded successfully! ===${NC}"
echo -e "${BLUE}Assets saved to: ${DRAWABLE_DIR}${NC}\n"
echo -e "${BLUE}Next steps:${NC}"
echo -e "1. Update TapToPayPrompt.kt to use painterResource() instead of AsyncImage"
echo -e "2. Replace URLs with: painterResource(R.drawable.ic_payment_visa), etc."
echo -e "3. Remove Coil dependency from build.gradle.kts if not used elsewhere\n"
