# BRSDD Road Sign Detector — Android App

A native Android app (Kotlin + Jetpack Compose, Material 3) for real-time, fully offline
detection of Bangladeshi road signs, powered by a custom-trained YOLOv8n model exported to
TensorFlow Lite.

## Features

- **Live camera detection** (CameraX) with bounding boxes, class labels, confidence, FPS and
  inference latency overlay.
- **Photo detection** — pick an image from the gallery and run the same model on it.
- **Adjustable confidence / IoU thresholds** and a GPU-delegate toggle in the in-app settings sheet.
- **About screen** with dataset credit, developer & instructor info, and validation metrics.
- 100% on-device inference — no network calls, no data leaves the device.

## Opening the project

1. Open **Android Studio** (Koala/2024.1 or newer recommended).
2. **File → Open** → select this folder (`BRSDDApp/`).
3. Let Gradle sync. If prompted about the Gradle wrapper, choose to let Android Studio use its
   bundled Gradle, or let it regenerate `gradlew`/`gradlew.bat` — the pinned version is Gradle 8.7
   (`gradle/wrapper/gradle-wrapper.properties`), compatible with Android Gradle Plugin 8.5.2.
4. Run on a physical device or emulator with a working camera (**API 26+**). A physical device is
   strongly recommended for the live-camera tab.

## Project details

| | |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Min SDK | 26 |
| Target SDK | 34 |
| Package | `com.rmstu.brsdd` *(rename freely — see below)* |
| Model | `app/src/main/assets/yolov8n.tflite` — input `[1,640,640,3]` float32, output `[1,33,8400]` float32 |
| Labels | `app/src/main/assets/labels.txt` — 29 classes, index order reconstructed from the validation report |

> ⚠️ Double-check `labels.txt` against your training `data.yaml` `names:` list before relying on
> the app's output in production — if the order doesn't match what the model was trained with,
> boxes will still be correct but labels may be shuffled.

## Renaming the package / app name

- App display name: `app/src/main/res/values/strings.xml` → `app_name`.
- Package name: currently `com.rmstu.brsdd` in `app/build.gradle.kts` (`namespace`, `applicationId`)
  and the `java/com/rmstu/brsdd/` folder structure. Use Android Studio's **Refactor → Rename**
  on the package for a safe rename across all files.

## Credits (editable in `AboutScreen.kt`)

- **Developer:** Arindam Datta — BSc CSE (3rd Year), Rangamati Science and Technology University (RMSTU)
- **Course Instructor:** G M Sakhawat Hossain, Assistant Professor — gmsakhawat@rmstu.ac.bd
- **Dataset:** BRSDD — Bangladeshi Road Sign Detection Dataset

## App release

- [download](https://github.com/ar1nd0m/BRSDD-App/raw/main/app/build/outputs/apk/debug/app-debug.apk)

## Known limitations / possible next steps

- Model loading currently happens synchronously on first composition (fast for this model size,
  but could be moved to a coroutine + loading spinner for a smoother cold start).
- The GPU-delegate toggle in Settings is wired up in the UI but currently requires an app restart
  to take effect (switching delegates means re-creating the `Interpreter`); hooking it up to a
  live reload is a good next enhancement.
- No unit/instrumented tests are included yet — `NmsUtils` and the output decoder in
  `RoadSignDetector` are the highest-value targets for test coverage.
