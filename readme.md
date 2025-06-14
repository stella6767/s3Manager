# S3 Manager 

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9.25-7F52FF?logo=kotlin" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.4.6-6DB33F?logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/JPA-Hibernate-59666C?logo=hibernate" alt="JPA"/>
  <img src="https://img.shields.io/badge/htmx-2-0D47A1?logo=htmx" alt="htmx"/>
  <img src="https://img.shields.io/badge/AWS_S3-SDK_v2-232F3E?logo=amazons3" alt="AWS S3"/>
  <img src="https://img.shields.io/badge/Infrastructure-GCP-4285F4?logo=googlecloud" alt="GCP"/>
</p>

`Kotlin`, `Spring Boot`와 `htmx`를 기반으로 Amazon S3 버킷의 객체를 관리하는 웹 애플리케이션입니다. 서버 중심의 현대적인 하이퍼미디어 아키텍처를 지향하며, 효율적인 파일 관리 및 업로드/다운로드 기능을 제공합니다.

---

## 📋 주요 기능

* **S3 연동 및 파일 브라우징**: 사용자의 S3 키를 등록하여 특정 버킷의 객체 및 폴더 목록을 조회합니다.
* **파일 업로드/다운로드**: 파일 크기에 따라 단일/멀티파트 업로드를 지원하며, 사전 서명된 URL(Pre-signed URL)을 통해 안전하고 효율적인 다운로드 기능을 제공합니다.
* **동적 UI**: **htmx**를 사용하여 페이지 새로고침 없이 서버로부터 HTML 조각을 받아 동적으로 UI를 업데이트합니다.
* **보안**: Spring Security와 OAuth2 클라이언트를 통한 사용자 인증 및 인가를 처리합니다.
* **동적 쿼리**: Kotlin JDSL을 사용하여 타입-세이프한 동적 JPA 쿼리를 생성합니다.

---

## 🛠️ 기술 스택 및 주요 라이브러리

### **Backend**

* **Language**: `Kotlin 1.9.25`
* **Framework**: `Spring Boot 3.4.6`
    * `Spring Web`
    * `Spring Security` (with `OAuth2 Client`)
    * `Spring Data JPA`
* **Database**:
    * `MySQL`
    * **QueryDSL 대안**: `Kotlin JDSL 3.5.4` (타입-세이프한 코틀린 전용 JPA/JPQL 빌더)
    * **SQL 로깅**: `p6spy 1.9.0` (실행되는 SQL 쿼리와 파라미터를 로그로 확인)
* **Cloud**:
    * `AWS SDK for Java v2` (S3, S3 Transfer Manager, S3 Presigner)

### **Frontend**

* **Core**: `htmx 2` (서버 주도형 동적 UI 구현)
* **Template Engine**: `JTE (Java Template Engine) 3.1.16` (빠른 성능의 컴파일 기반 템플릿 엔진)
* **CSS Framework**: `Tailwind CSS 4.0`
* **UI Component Library**: `daisyUI` (Tailwind CSS 기반 컴포넌트 라이브러리)

### **Infrastructure**

* `GCP (Google Cloud Platform) Compute Engine VM + SQL`

### **Build & Tools**

* **Build Tool**: `Gradle`
* **Java Version**: `Java 21`
* **Utilities**:
    * `kotlin-logging`: 로깅 퍼사드
    * `commons-lang3`: 범용 유틸리티 라이브러리

### **Testing**

* `JUnit 5`
* `Spring Boot Test`
* `Spring Security Test`

