# 📱 투두두두 (Hide in Bash)

## 📝 프로젝트 개요
![캐릭터 애니메이션 시연](https://animatedoss.s3.amazonaws.com/fad01384-aeea-4539-946e-025387d43e81/video.gif)
투두두두는 자신만의 캐릭터를 제작하여, 자신의 목표를 게임의 퀘스트처럼 진행하는 자신만의 커스텀 투두리스트입니다.

이 프로젝트는 [Facebook Research의 AnimatedDrawings](https://github.com/facebookresearch/AnimatedDrawings) 라이브러리를 기반으로 애니메이션 기능을 구현하였습니다.


---

## 🧰 기술 스택
- **언어:** Kotlin
- **UI 프레임워크:** XML  
- **라이브러리:**  
  - Retrofit (📡 API 통신)  
  - Room (🗂️ 로컬 DB)  
  - Coroutine / Flow (🔄 비동기 처리)  
  - Glide(🖼️ 이미지 로딩)
  - Canvas(이미지 인식 스켈레톤 생성)
- **아키텍처**: SAA (Single Activity Architecture)

---

## 🎯 MVP 핵심 기능

- 📝 **할 일 등록 및 완료 기능**  
  사용자는 To-Do 항목을 등록하고 완료할 수 있으며, 모든 데이터는 로컬 Room DB를 통해 관리됩니다.  
  할 일 완료 시, 애니메이션과 연동되어 동기부여를 제공합니다.

- 🧬 **경험치 기반 레벨 시스템**  
  사용자의 활동(할 일 완료)에 따라 경험치가 쌓이고, 일정 기준 도달 시 캐릭터가 레벨업합니다.  
  경험치 및 레벨 데이터 역시 Room DB를 통해 지속적으로 관리됩니다.

- 👤 **커스터마이징 가능한 캐릭터 생성**  
  사용자는 자신의 캐릭터를 선택하거나 커스터마이징할 수 있으며, 생성 요청은 자체 구축한 Flask 서버의 API를 통해 처리됩니다.
  Flask API 서버는 meta의 오픈 소스를 사용하여 커스텀하여 제작하였습니다.

- 🎭 **할 일 피드백 애니메이션 연출**  
  사용자가 할 일을 완료하지 못했을 경우, 캐릭터가 쓰러지는 애니메이션을 통해 시각적인 피드백을 제공합니다.  
  이를 통해 재미 요소를 더하고 사용자 습관 형성을 유도합니다.

---

## 🚀 향후 확장 고려 기능

- 🦴 **사용자 지정 뼈대 제작 기능**  
  사용자가 직접 캐릭터의 뼈대를 설정하여, 더욱 정교한 커스터마이징 애니메이션 제작이 가능하도록 개선할 예정입니다.

- 🎥 **모션 캡처 기반 캐릭터 애니메이션 제작**  
  스마트폰 카메라 또는 외부 센서를 이용해, 사용자가 자신의 동작을 기반으로 주인공 및 적 캐릭터의 애니메이션을 직접 제작할 수 있는 기능을 도입할 계획입니다.

- 🤖 **ChatGPT 기반 퀘스트 자동 생성 기능**  
  사용자의 일정이나 목표를 분석하여, ChatGPT를 통해 개인화된 할 일(퀘스트)을 자동으로 생성해주는 기능을 추가할 예정입니다.

- 🛡️ **레벨업 연동 캐릭터 장비 변화**  
  레벨업 시 캐릭터의 외형 및 장비(예: 무기, 방어구 등)가 자동으로 변경되어, 시각적 보상 요소와 성장의 재미를 강화할 예정입니다.

---

## 🛤 프로젝트 진행 방법
- GitHub Flow 방식 사용  
- 이슈 트래킹: GitHub Issues + Projects 사용  
- 커밋 메시지 컨벤션: Conventional Commits  
  > 예: `[Feat] 게시물 생성 기능 추가`

---

