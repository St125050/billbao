# BillBao (บิลเบา) — Edge AI Restaurant Expense & Tax Ledger

<p align="center">
  <img src="app/src/main/res/drawable/app_logo.png" width="140" alt="BillBao Logo" />
</p>

<p align="center">
  <b>100% On-Device Edge AI • Privacy-First Financial Accounting • Zero Cloud Dependency</b><br>
  <i>"Scan Bills. Save Tax. Clean Ledger."</i>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android_8.0+_(API_26--36)-00E599?style=flat-square&logo=android" alt="Android" />
  <img src="https://img.shields.io/badge/Edge_AI-Google_ML_Kit-blue?style=flat-square&logo=google" alt="ML Kit" />
  <img src="https://img.shields.io/badge/Language-Kotlin_2.3-7F52FF?style=flat-square&logo=kotlin" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4?style=flat-square&logo=jetpackcompose" alt="Compose" />
  <img src="https://img.shields.io/badge/Database-Local_SQLite-003B57?style=flat-square&logo=sqlite" alt="SQLite" />
  <img src="https://img.shields.io/badge/Privacy-100%25_On--Device-success?style=flat-square" alt="Privacy" />
</p>

---

## 🌟 Overview

**BillBao (บิลเบา)** is a native Android expense intelligence and tax compliance platform engineered specifically for restaurants, commercial kitchens, chefs, and food service entrepreneurs in Thailand and Southeast Asia. 

While traditional accounting tools rely on cloud APIs that leak proprietary supplier pricing, require recurring SaaS fees, and fail inside wet markets and kitchen basements with poor connectivity, **BillBao executes its entire computer vision, natural language extraction, and report compilation pipelines directly on the edge**.

---

## 🧠 Elevating Edge AI: Why On-Device Intelligence Matters
Financial receipts and supplier invoices contain a restaurant's most sensitive trade secrets: supplier costs, raw ingredient wholesale rates, wholesale discounts, and net margins. BillBao is designed from the ground up on the **Edge AI Privacy-First Paradigm**:

flowchart TD
    A["📸 Physical Receipt / Market Slip"] --> B["⚡ Camera / Photo Picker"]
    B --> C["🤖 On-Device Google ML Kit OCR (libmlkit_google_ocr_pipeline.so)"]
    C --> D["🧠 Edge NLP & Financial Heuristic Engine"]
    
    subgraph Edge Intelligence ["On-Device Edge Intelligence Pipeline"]
        D --> E1["🏢 Vendor Entity Extractor"]
        D --> E2["🆔 13-Digit Tax ID (TIN) Extractor"]
        D --> E3["📅 Buddhist Era (B.E.) / Gregorian Date Parser"]
        D --> E4["💰 Thai Dash ('160 -') & Multilingual Total Parser"]
        D --> E5["🏷️ Zero-Shot Contextual Category Classifier"]
        D --> E6["🎯 Candidate Number Ranker"]
    end
    
    Edge Intelligence --> F["💾 Local SQLite Engine (billbao.db)"]
    F --> G["📱 Jetpack Compose UI (Obsidian Dark Theme)"]
    F --> H["📄 On-Device A4 PDF Engine (android.graphics.pdf)"]
    F --> I["📊 Direct RFC 4180 CSV / Excel Exporter"]


### 1. Zero Cloud Latency & Total Privacy
* **Offline Inference**: ML Kit's edge OCR pipeline (`libmlkit_google_ocr_pipeline.so`) runs directly on the device's CPU/GPU/NPU.
* **Zero Cloud Data Leakage**: Sensitive tax invoices (`ใบกำกับภาษี`) and market cash receipts (`บิลเงินสด`) never leave the phone.
* **Zero Subscription Fees**: No per-receipt cloud API bills (Google Cloud Vision, AWS Textract, Azure Document Intelligence).

### 2. Edge Financial Heuristic Engine (`ReceiptOcrParser.kt`)
* **Entity Extraction**: Distinguishes true merchant titles from generic invoice metadata (`Tax Invoice`, `Receipt`, `Welcome`, `ยินดีต้อนรับ`).
* **Tax Identification Number (TIN)**: Automatically recognizes and validates the Thai Revenue Code 13-digit standard (`เลขประจำตัวผู้เสียภาษี`).
* **Thai & English Total Amount Parsing**:
  * **Priority 1 (Semantic Keywords)**: Evaluates lines containing `Total`, `Grand Total`, `Amount Due`, `ยอดรวม`, `รวมทั้งสิ้น`, `รวมเงิน`, `จำนวนเงินทั้งสิ้น`, `ยอดสุทธิ`.
  * **Priority 2 (Thai Dash Notation)**: Accurately identifies colloquial Thai handwritten or stamped dash amounts (e.g. `160 -`, `720.-`, `1,250 -`).
  * **Priority 3 (Candidate Chips)**: Surfaces all discovered numbers as 1-tap candidate chips for instant selection.
* **Zero-Shot Restaurant Accounting Classifier**: Contextually maps raw text tokens into standard chart of accounts:
  * `Food Supplies (COGS)` (fresh markets, meat, vegetables, seafood)
  * `Cooking Gas & Utilities` (LPG tanks, electricity, water)
  * `Kitchen Equipment (Capex)` (refrigerators, freezers, fryers, stoves)
  * `Packaging & Disposables` (takeaway boxes, bags, cups)
  * `Repairs & Upkeep` (technician services, maintenance)
  * `Staff Meals & Shifts`

---

## 🚀 Key Features

* **🌱 Clean Slate ฿0.00 Fresh Start**:
  * The ledger opens at strictly **`฿0.00 THB`** with `0 Bills`. No seed data or mock transactions.
  * All ledger summaries, tax shields, and earlier monthly reports are dynamically derived strictly from real bills in SQLite.
* **🛡️ Thai Revenue Code Tax Shield (Section 65 Ter)**:
  * Automatically calculates deductible business expenses under Section 65 Ter.
  * Calculates estimated **7% Input VAT Reclaim (`PP.30`)** to offset restaurant sales tax.
* **📄 On-Device A4 Monthly PDF Audit Reports**:
  * Compiled directly on device using `android.graphics.pdf.PdfDocument`.
  * **Month-over-Month (MoM) Comparison**: Displays percentage variance and amount changes (`▲ +฿15,200 (+25.3%) Increased` or `▼ -฿8,500 (-12.4%) Decreased / Savings`).
  * **Canvas-Drawn Charts**: Side-by-side MoM comparative bar charts and category breakdown graphs.
  * Multi-page pagination, official metadata, and itemized ledger tables.
* **📊 Direct Excel & CSV Downloads**:
  * Generates RFC 4180 CSV files with **UTF-8 Byte Order Mark (`\uFEFF`)** saved directly to public `Downloads/`.
  * Opens natively in Microsoft Excel, Google Sheets, and WPS Office with flawless Thai and English character rendering.
* **👨‍🍳 Profile & Business Customization**:
  * Customizable business name, 13-digit Tax ID, branch number, registered address, and currency symbol (`THB ฿`).

---

## 🛠️ Architecture & Tech Stack

| Component | Technology | Description |
|---|---|---|
| **Language** | Kotlin 2.3.20 | 100% Modern Kotlin with Coroutines & Flow |
| **Edge AI Engine** | Google ML Kit Text Recognition | On-device tensor vision pipeline (`16.0.1`) |
| **UI Framework** | Jetpack Compose & Material 3 | Declarative UI with obsidian dark/light themes |
| **Persistence** | SQLite (`billbao.db`) | Local relational storage with zero-latency queries |
| **Architecture** | MVVM + StateFlow | Reactive unidirectional data flow |
| **Document Engine** | Android Canvas & `PdfDocument` | Hardware-accelerated vector PDF generation |
| **Interoperability** | `FileProvider` & MediaStore | Android 10+ scoped storage & download integration |

---

## 📂 Project Structure

```
billbao/
├── app/
│   ├── build.gradle.kts                # Android SDK 36, ML Kit & Compose config
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml     # Camera permissions, FileProvider
│       │   ├── kotlin/com/billbao/app/
│       │   │   ├── MainActivity.kt     # Single-activity Jetpack Compose entry
│       │   │   ├── data/
│       │   │   │   ├── database/       # SQLite helper (bills & profile tables)
│       │   │   │   ├── export/         # On-Device PDF & Excel/CSV compilers
│       │   │   │   ├── model/          # Bill, MonthlyReport, UserProfile models
│       │   │   │   ├── ocr/            # ReceiptOcrParser (Edge AI Vision & NLP)
│       │   │   │   └── repository/     # StateFlow repository layer
│       │   │   └── ui/
│       │   │       ├── main/           # MainScreen, ScanBillSheet, TaxShieldScreen
│       │   │       └── theme/          # Obsidian dark neon color palette
│       │   └── res/                    # Drawables, launcher mipmaps, FileProvider xml
└── README.md
```

---

## 🏗️ Building from Source

### Prerequisites
* **Android Studio Ladybug (2024.2+)** or **Android CLI**
* **JDK 17** (Eclipse Temurin or OpenJDK 17)
* Android SDK Platform 36 & Build-Tools 36.0.0

### Build Commands
```bash
# Clone the repository
git clone https://github.com/St125050/billbao.git
cd billbao

# Build Debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License & Attribution

Developed with passion for restaurant owners and chefs. Engineered with modern Android & Edge AI technologies.
