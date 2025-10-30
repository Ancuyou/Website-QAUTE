# QAUTE - Website Tư Vấn Sinh Viên Trực Tuyến

![Java](https://img.shields.io/badge/Java-21-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-teal.svg)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-purple.svg)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP%20%26%20SockJS-blueviolet.svg)

**QAUTE** là một nền tảng web được xây dựng nhằm mục đích kết nối sinh viên với các tư vấn viên chuyên nghiệp và tạo ra một cộng đồng hỗ trợ lẫn nhau trong học tập và phát triển cá nhân. Dự án được phát triển bằng **Spring Boot** cho backend và **Thymeleaf** kết hợp **Bootstrap** cho frontend.

**Link Website**
https://moses-unsophistical-unmusically.ngrok-free.dev/qaute/auth/login


## Mục Lục

* [Giới thiệu](#giới-thiệu-)
* [Tính Năng Chính](#tính-năng-chính-)
* [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng-)
* [Cài Đặt và Chạy Dự Án (Local)](#cài-đặt-và-chạy-dự-án-local-)
* [Cấu Trúc Dự Án](#cấu-trúc-dự-án-)
* [Đóng Góp](#đóng-góp-)
* [Liên Hệ](#liên-hệ-)

---

## Giới Thiệu 🚀

QAUTE giải quyết nhu cầu tìm kiếm thông tin và tư vấn của sinh viên trong môi trường đại học. Nền tảng cung cấp một không gian để:

* **Sinh viên:** Đặt câu hỏi, tìm kiếm thông tin, tham gia cộng đồng hỏi đáp, chat trực tiếp với tư vấn viên, tham gia các sự kiện/workshop.
* **Tư vấn viên (Consultant):** Trả lời câu hỏi, chat hỗ trợ sinh viên, tạo và quản lý sự kiện/workshop.
* **Quản lý (Manager):** Duyệt câu hỏi, quản lý lĩnh vực, duyệt sự kiện, xử lý báo cáo nội dung xấu, xem thống kê.
* **Quản trị viên (Admin):** Quản lý tài khoản (User, Consultant, Manager), quản lý khoa/ngành, quản lý phiên đăng nhập, quản lý thông báo hệ thống.

---

## Tính Năng Chính ✨

* **Xác thực & Phân quyền:**
    * Đăng ký, Đăng nhập (thông thường & Google OAuth2).
    * Quên mật khẩu (OTP qua email).
    * Phân quyền rõ ràng cho 4 vai trò: User, Consultant, Manager, Admin.
    * **MFA (Multi-Factor Authentication)** cho Admin & Manager (OTP Email + PIN).
    * Quản lý phiên đăng nhập (Refresh Token).
    * **Bảo mật đăng nhập:** Giới hạn số lần đăng nhập sai, khóa tài khoản tạm thời (theo cấp độ), khóa thiết bị đáng ngờ.
* **Cộng Đồng Hỏi Đáp:**
    * Đặt câu hỏi (tiêu đề, nội dung, khoa/ngành, đính kèm file).
    * Tư vấn viên trả lời câu hỏi.
    * Hiển thị câu hỏi theo khoa, lĩnh vực.
    * Tìm kiếm, lọc và sắp xếp câu hỏi.
    * **Tương tác:** Like câu hỏi, xem số lượt xem.
    * **Chỉnh sửa câu hỏi** (chỉ user trước khi có trả lời/like).
    * **Thu hồi câu trả lời** (chỉ consultant).
    * **Báo cáo nội dung xấu** (Câu hỏi & Câu trả lời).
* **Chat Trực Tiếp (Real-time):**
    * User chat với Consultant.
    * Sử dụng WebSocket (STOMP qua SockJS).
    * Lưu trữ lịch sử chat.
    * Hiển thị danh sách người đã chat.
    * **Hỗ trợ AI:** Tự động trả lời khi Consultant offline (có thể tắt bởi Manager).
    * **Thu hồi tin nhắn** (trong thời gian giới hạn).
    * **Feedback cho tin nhắn AI**.
* **Quản lý Sự Kiện & Workshop:**
    * Consultant tạo sự kiện (Tiêu đề, mô tả, loại, hình thức, thời gian, địa điểm/link, số lượng, banner, khoa/lĩnh vực).
    * Manager duyệt/từ chối sự kiện.
    * User xem danh sách, chi tiết và đăng ký tham gia sự kiện.
    * User quản lý các sự kiện đã đăng ký, hủy đăng ký (trước hạn).
    * Consultant/Manager xem danh sách người tham gia.
    * Consultant điểm danh (đánh dấu đã tham dự/vắng mặt).
    * User đánh giá sự kiện sau khi tham dự.
    * **Thông báo tự động:** Nhắc nhở sự kiện (24h, 15p trước), sự kiện sắp đầy, đăng ký thành công, sự kiện bị hủy,...
* **Thông Báo:**
    * Thông báo real-time qua WebSocket.
    * Thông báo qua Email (cho các sự kiện quan trọng hoặc khi người dùng offline).
    * Admin/Manager gửi thông báo đến các nhóm người dùng (ALL, User, Consultant, Manager).
    * Quản lý thông báo (Thêm, sửa, xóa, xem trước).
* **Quản lý (Manager):**
    * Duyệt câu hỏi.
    * Quản lý Lĩnh vực (Field).
    * Duyệt/Từ chối/Hủy Sự kiện.
    * Xem báo cáo thống kê (Câu hỏi, Câu trả lời, User, Consultant).
    * Xử lý nội dung xấu (Từ báo cáo người dùng & Lọc tự động của AI).
    * Gửi thông báo.
* **Quản trị (Admin):**
    * Quản lý tài khoản (User, Consultant, Manager): Thêm, sửa, khóa/mở khóa, xóa.
    * Quản lý Khoa/Ngành (Department).
    * Quản lý phiên đăng nhập (Xem, thu hồi, dọn dẹp token hết hạn).
    * Gửi thông báo hệ thống.
* **Giao Diện Người Dùng:**
    * Sử dụng Thymeleaf và Bootstrap 5.
    * Responsive design.
    * Tích hợp các thư viện JavaScript (Chart.js, Lottie).
    * Cập nhật một phần giao diện bằng AJAX (Fetch API).

---

## Công Nghệ Sử Dụng 🛠️

* **Backend:**
    * **Ngôn ngữ:** Java 21
    * **Framework:** Spring Boot 3.5.5
    * **Database:** MySQL
    * **ORM:** Spring Data JPA / Hibernate
    * **Security:** Spring Security, JWT (Nimbus JOSE+JWT), OAuth2 Client (Google)
    * **Template Engine:** Thymeleaf
    * **Real-time:** Spring WebSocket, STOMP, SockJS
    * **Email:** Spring Mail
    * **Caching:** Spring Cache, Caffeine
    * **File Storage:** Cloudinary
    * **API Client:** Spring WebFlux (WebClient)
    * **Scheduling:** Spring Task Scheduling (@Scheduled)
    * **Utilities:** Lombok
* **Frontend:**
    * **Framework/Library:** Thymeleaf (Server-side rendering)
    * **Styling:** Bootstrap 5
    * **Icons:** Font Awesome, Bootstrap Icons
    * **JavaScript:** Vanilla JS, Fetch API, SockJS, STOMP Client
    * **Charting:** Chart.js
    * **Animation:** Lottie (for animations)
* **AI Integration:**
    * FastAPI (External services for chat response and toxic content detection)
* **Build Tool:** Maven

---

## Cài Đặt và Chạy Dự Án (Local) 💻

**Yêu cầu:**

* JDK 21 hoặc mới hơn
* Maven 3.6+
* MySQL Server 8.0+

**Các bước cài đặt:**

1.  **Clone Repository:**
    ```bash
    git clone https://github.com/Ancuyou/Website-QAUTE.git
    cd Website-QAUTE 
    ```
2.  **Cấu hình Database:**
    * Tạo một database MySQL (ví dụ: `qaute`).
    * Mở file `src/main/resources/application.yaml`.
    * Cập nhật thông tin `spring.datasource.url`, `username`, `password` cho phù hợp với môi trường của bạn.
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/qaute # Thay đổi nếu cần
        username: root # Thay bằng username MySQL của bạn
        password: your_password # Thay bằng password MySQL của bạn
    ```
3.  **Cấu hình Biến Môi Trường (Quan trọng):**
    * Dự án sử dụng biến môi trường cho các thông tin nhạy cảm. Bạn cần cung cấp các biến này (có thể đặt trong file `.env` ở thư mục gốc hoặc cấu hình trong IDE):
        * `GOOGLE_CLIENT_ID`: ID client Google OAuth2.
        * `GOOGLE_CLIENT_SECRET`: Secret client Google OAuth2.
        * (Có thể có các biến khác tùy thuộc vào cấu hình đầy đủ)
    * **Lưu ý:** Nếu không cấu hình Google OAuth2, tính năng đăng nhập bằng Google sẽ không hoạt động.
4.  **Cấu hình Email:**
    * Mở file `src/main/resources/application.yaml`.
    * Cập nhật `spring.mail.username` và `password` bằng tài khoản Gmail của bạn (cần bật "Less secure app access" hoặc sử dụng "App Passwords").
5.  **Cấu hình Cloudinary:**
    * Mở file `src/main/resources/application.yaml`.
    * Cập nhật `cloudinary.cloud_name`, `api_key`, `api_secret` bằng thông tin tài khoản Cloudinary của bạn để lưu trữ ảnh đại diện, banner sự kiện, file đính kèm.
6.  **Cấu hình AI Services:**
    * Mở file `src/main/resources/application.yaml`.
    * Cập nhật `ai.chat.base-url` và `ai.toxic.base-url` nếu bạn có các dịch vụ AI riêng đang chạy. Nếu không, các tính năng liên quan đến AI (chat tự động, lọc nội dung xấu) có thể không hoạt động đúng.
7.  **Build và Chạy:**
    * **Cách 1: Sử dụng Maven Wrapper (khuyến nghị):**
        ```bash
        # Trên Linux/macOS
        ./mvnw clean spring-boot:run

        # Trên Windows
        .\mvnw.cmd clean spring-boot:run
        ```
    * **Cách 2: Sử dụng Maven cài đặt sẵn:**
        ```bash
        mvn clean spring-boot:run
        ```
    * **Cách 3: Build file JAR và chạy:**
        ```bash
        mvn clean package
        java -jar target/QAUTE-0.0.1-SNAPSHOT.jar
        ```
8.  **Truy cập ứng dụng:**
    * Mở trình duyệt và truy cập: `http://localhost:8080/qaute`

---

## Cấu Trúc Dự Án 📁
QAUTE/

├── .mvn/                      # Maven Wrapper files

├── src/

│   ├── main/

│   │   ├── java/it/ute/QAUTE/ # Source code chính

│   │   │   ├── api/           # API clients (FastAPI)

│   │   │   ├── configuration/ # Cấu hình Spring (Security, Cache, WebSocket, ...)

│   │   │   ├── controller/    # Controllers xử lý request HTTP

│   │   │   ├── dto/           # Data Transfer Objects

│   │   │   ├── entity/        # JPA Entities (Database models)

│   │   │   ├── exception/     # Custom exceptions và handlers

│   │   │   ├── repository/    # Spring Data JPA repositories

│   │   │   ├── service/       # Interfaces dịch vụ

│   │   │   │   └── Implement/ # Implementations dịch vụ

│   │   │   └── web/           # Web components (Interceptors, Handlers)

│   │   └── resources/

│   │       ├── static/        # Tài nguyên tĩnh (CSS, JS, Images)

│   │       ├── templates/     # Thymeleaf templates (HTML views)

│   │       │   ├── fragments/ # Reusable UI components

│   │       │   └── pages/     # Specific pages for roles (admin, consultant, manager, user)

│   │       └── application.yaml # Cấu hình ứng dụng Spring Boot

│   └── test/                  # Unit and integration tests

├── .gitattributes             # Git configuration

├── .gitignore                 # Files ignored by Git

├── mvnw                       # Maven Wrapper script (Linux/macOS)

├── mvnw.cmd                   # Maven Wrapper script (Windows)

├── pom.xml                    # Maven project configuration

└── README.md                  # Tài liệu này 

---

## Đóng Góp 🤝

Hiện tại, dự án chưa mở cho đóng góp từ cộng đồng. Tuy nhiên, bạn có thể fork repository và tự do phát triển.

---

## Liên Hệ 📧

Nếu có bất kỳ câu hỏi hoặc góp ý nào, vui lòng liên hệ:

* **Thành viên:**
    * Nguyễn Ngọc Thái Bảo - 23110180 - Email: 23110180@student.hcmute.edu.vn
    * Đăng Ngọc Nhân - 23110279 - Email: 23110279@student.hcmute.edu.vn
    * Huỳnh Duy Nguyễn - 23110274 - Email: 23110274@student.hcmute.edu.vn
    * Trần Hồng Quang Lê - 23110251 - Email: 23110251@student.hcmute.edu.vn




