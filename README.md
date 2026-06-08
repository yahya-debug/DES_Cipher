# Enhanced DES Secure Communication & File Protection System

A Java implementation of the Data Encryption Standard (DES) with secure communication features — send encrypted data via email and DES keys via SMS.

## Features

- **Key Expansion** — Visualize the full DES key schedule (PC-1, C0/D0 splits, all 16 round subkeys)
- **Encrypt/Decrypt Text** — DES encryption/decryption with PKCS padding, hex output
- **Encrypt/Decrypt Files** — File-level DES encryption (`.des`) and decryption (`.dec`)
- **Send Ciphertext via Email** — Deliver encrypted data through Gmail SMTP
- **Send DES Key via SMS** — Transmit keys via Twilio SMS API
- **Random Key Generation** — Generate random 64-bit DES keys

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8+
- `javax.mail.jar`, `activation.jar` (from `lib/`)
- `twilio.jar` (from `lib/`)
- Twilio account (for SMS)
- Gmail account with App Password (for email)

### Environment Setup

```bash
cp .env.example .env
```

Edit `.env` with your credentials:

```ini
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_FROM_NUMBER=+your_twilio_number

EMAIL_USERNAME=your_email@gmail.com
EMAIL_PASSWORD=your_app_password
```

Load the environment variables:

```bash
export $(grep -v '^#' .env | xargs)
```

### Compile & Run

```bash
javac -cp "lib/*" -d out src/*.java
java -cp "out:lib/*" Main
```

## Usage

```
===== Enhanced DES Secure Communication System =====
1. Key Expansion
2. Encrypt Text
3. Decrypt Text
4. Encrypt File
5. Decrypt File
6. Send Ciphertext via Email
7. Send DES Key via SMS
8. Generate Random DES Key
9. Exit
```

## Project Structure

```
src/
  Main.java            -- CLI menu interface
  DES.java             -- Core DES algorithm implementation
  DataUtils.java       -- Conversion and utility functions
  FileProcessor.java   -- File encryption/decryption
  EmailService.java    -- Email via Gmail SMTP
  SMSService.java      -- SMS via Twilio SDK
  README.md
  .env.example
  .gitignore
lib/
  javax.mail.jar
  activation.jar
  twilio.jar
```

## DES Implementation

### Key Scheduling
1. 64-bit key → PC-1 permutation → 56 bits
2. Split into two 28-bit halves (C0, D0)
3. 16 rounds of left-shifts (1 or 2 bits per round)
4. Each shifted pair → PC-2 permutation → 48-bit subkey

### Encryption
1. Initial Permutation (IP)
2. Split into L0 (32 bits) and R0 (32 bits)
3. 16 Feistel rounds: Li = R_{i-1}, Ri = L_{i-1} ⊕ f(R_{i-1}, Ki)
4. Final Permutation (IP^{-1})

### f-Function
- Expansion (E): 32 → 48 bits
- XOR with round subkey
- S-Box substitution (S1–S8): 6→4 bits each
- P-Box permutation

### Decryption
Same as encryption with reversed round keys (K16 → K1).

## Test Vector

```
Key:          133457799BBCDFF1
Plaintext:    0123456789ABCDEF
Ciphertext:   85E813540F0AB405
```

Passes the NIST test vector and 100 random test cases.
