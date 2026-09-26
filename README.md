# 🎬 PromptFlow AI — AI Video Master Prompt Generator

<p align="center">
  <strong>Create Professional AI Video Prompts for Google Flow, Sora, Runway, Kling & Luma</strong>
</p>

---

## 📱 How to Download APK from GitHub / গিটহাব থেকে APK ডাউনলোড করার উপায়

### ধাপ ১: GitHub Release এ যান
1. এই GitHub পেজের ডানপাশে (Right Sidebar) **Releases** সেকশনে যান অথবা সরাসরি **[Releases](../../releases)** লিংকে ক্লিক করুন।
2. সর্বশেষ রিলিজ (Latest Release) দেখতে পাবেন।

### ধাপ ২: APK ডাউনলোড করুন
- **Assets** সেকশনের নিচে **`PromptFlow-AI.apk`** ফাইলে ক্লিক করলেই APK ডাউনলোড শুরু হবে।

### ধাপ ৩: মোবাইলে ইনস্টল করুন
1. ডাউনলোড সম্পন্ন হলে নোটিফিকেশন বা ফাইল ম্যানেজার থেকে **`PromptFlow-AI.apk`** ফাইলটি ওপেন করুন।
2. প্রথমবার ইনস্টল করার সময় অ্যান্ড্রয়েড ফোন থেকে **"Install unknown apps"** অনুমতি চাইতে পারে। অনুমতি (Allow) দিন।
3. **Install** বাটনে চাপ দিন। ব্যস, অ্যাপটি আপনার ফোনে ইনস্টল হয়ে যাবে!

---

## ⚙️ How the Automatic GitHub Release Works (গিটহাব অটোমেশন)

এই রিপোজিটরিতে **GitHub Actions CI/CD Workflow** (`.github/workflows/build-and-release.yml`) যুক্ত করা আছে:

1. **অটোমেটিক বিল্ড ও রিলিজ (Automatic on Push):**
   - আপনি যখন এই কোডটি GitHub-এ পুশ করবেন (`main` বা `master` ব্রাঞ্চে) অথবা নতুন ট্যাগ পুশ করবেন (যেমন `v1.0.0`), GitHub Actions নিজে থেকেই ক্লাউডে Android APK বিল্ড করবে।
   - বিল্ড সম্পন্ন হওয়ার পর স্বয়ংক্রিয়ভাবে একটি **GitHub Release** তৈরি হবে এবং তাতে **`PromptFlow-AI.apk`** ফাইলটি আপলোড হয়ে যাবে।

2. **ম্যানুয়াল রিলিজ তৈরি (Manual Trigger):**
   - GitHub রিপোজিটরির **Actions** ট্যাবে গিয়ে **"Build & Release Android APK"** সিলেক্ট করুন।
   - **"Run workflow"** বাটনে ক্লিক করলে কিছুক্ষণের মধ্যে নতুন রিলিজ ও APK তৈরি হয়ে যাবে।

3. **Artifacts ডাউনলোড:**
   - যেকোনো বিল্ড রানের ভেতর ঢুকলে **Artifacts** সেকশনেও `PromptFlow-AI-APK` জিপ আকারে পাওয়া যাবে।

---

## ✨ Features (অ্যাপের মূল বৈশিষ্ট্যসমূহ)

- **AI Video Master Prompt Generation**: DeepSeek V4.1 Flash মডেল এবং XKIRO API এর মাধ্যমে সম্পূর্ণ প্রোডাকশন-রেডি ভিডিও প্রম্পট তৈরি।
- **Exact Dialogue Preservation**: বাংলা, ইংরেজি ও বাংলিশ যেকোনো স্ক্রিপ্টকে অক্ষত রেখে প্রম্পট তৈরি করে (কোনো অবাঞ্ছিত অনুবাদ বা সংক্ষিপ্তকরণ ছাড়াই)।
- **Presenter Reference Image**: ফটো পিক করার সুবিধা, যা মডেলের মুখাবয়ব, স্কিন টোন, পোশাক এবং চুলের স্টাইল শতভাগ বজায় রাখে।
- **22-Point Cinematography Directives**: ক্যামেরা অ্যাঙ্গেল, লাইটিং, একটানা ভয়েস টেক, লিপ-সিঙ্ক এবং সিনক্রোনাইজড বি-রোল ডিরেকশন।
- **Local + Firebase Cloud Firestore Database**: লোকাল রুম ডাটাবেসের পাশাপাশি ফায়ারবেস ক্লাউড ফায়ারস্টোরে প্রম্পট সিঙ্ক ও ব্যাকআপ।
- **Export & Share**: এক ক্লিকে ক্লিপবোর্ডে কপি, ইন-লাইন এডিট এবং `.txt` ফাইল হিসেবে ডাউনলোড বা শেয়ারের সুবিধা।

---

## 🛠️ Tech Stack

- **Platform:** Android (Min SDK 24, Target SDK 36)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material Design 3 (Dark/Light/System)
- **Local Persistence:** Room Database (KSP)
- **Cloud Database:** Firebase Cloud Firestore
- **Networking:** Retrofit, OkHttp, Moshi
- **CI/CD Automation:** GitHub Actions (`.github/workflows/build-and-release.yml`)
