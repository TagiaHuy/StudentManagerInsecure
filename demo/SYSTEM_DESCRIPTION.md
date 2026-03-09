# Tài liệu Mô tả Hệ thống Quản lý Sinh viên (Insecure Version)

## 1. Giới thiệu Tổng quan
Dự án **Student Manager Insecure** là một ứng dụng mô phỏng hệ thống quản lý giáo dục, được xây dựng có chủ đích chứa các lỗ hổng bảo mật phổ biến theo tiêu chuẩn **OWASP Top 10 (2021)**. Mục tiêu của dự án là phục vụ việc nghiên cứu, tấn công thực nghiệm và trình bày các phương án vá lỗi bảo mật trong lập trình Web với Java Spring Boot và ReactJS.

## 2. Công nghệ sử dụng
- **Backend**: Java 17, Spring Boot 4.0.3.
- **Database**: MySQL 8.0.
- **Data Access**: Spring JDBC Template (Native SQL nối chuỗi).
- **API Documentation**: Swagger UI (OpenAPI 3.0).
- **Frontend (Sample)**: ReactJS (Fetch API, LocalStorage).

## 3. Các Vai trò trong Hệ thống (Roles)
1. **User thường**: Tài khoản vừa đăng ký, chưa có quyền sinh viên/giảng viên.
2. **Sinh viên (ROLE_STUDENT)**: Có mã sinh viên (SVx), có thể đăng ký lớp và xem điểm.
3. **Giảng viên (ROLE_TEACHER)**: Có mã giảng viên (GVx), quản lý lớp học và nhập điểm.
4. **Quản trị viên (ROLE_ADMIN)**: Quyền cao nhất, phê duyệt tài khoản và quản lý nhân sự.

---

## 4. Các Luồng Nghiệp vụ Chính (Business Flows)

### Luồng 1: Đăng ký và Nâng cấp tài khoản
1. Người dùng thực hiện **Đăng ký** tài khoản mới qua API `/api/register`.
2. Sau khi đăng nhập, người dùng gửi **Yêu cầu cấp quyền sinh viên** qua `/api/students/request-role`.
3. Admin xem danh sách yêu cầu (`/api/students/pending-requests`) và nhấn **Phê duyệt** (`/api/students/approve-role`).
4. Hệ thống tự động gán quyền `ROLE_STUDENT` và sinh **Mã sinh viên (SVx)** cho người dùng.

### Luồng 2: Quản lý Đào tạo (Giảng viên & Admin)
1. Admin tạo tài khoản cho **Giảng viên** qua `/api/teachers` (Hệ thống tự sinh mã GVx).
2. Giảng viên hoặc Admin thực hiện **Tạo lớp học** mới qua `/api/classes`. Hệ thống tự sinh **Mã lớp (LHx)**.
3. Giảng viên cập nhật điểm số cho sinh viên qua `/api/students/update-grade`.

### Luồng 3: Học tập (Sinh viên)
1. Sinh viên thực hiện **Đăng ký vào lớp học** bằng mã lớp qua `POST /api/students`.
2. Sinh viên xem danh sách lớp mình đang theo học và điểm số tương ứng qua `/api/students/my-classes`.
3. Sinh viên có thể **Cập nhật ảnh đại diện** qua `/api/students/{id}/upload-avatar`.

---

## 5. Danh sách Module và API

### A. Module Xác thực (Authentication)
- `POST /api/register`: Tạo tài khoản (Lưu mật khẩu plaintext).
- `POST /api/login`: Đăng nhập, trả về Token Base64 và StudentCode/TeacherCode.
- `GET /api/logout`: Hủy phiên làm việc.

### B. Module Quản lý Sinh viên (Student Management)
- `POST /api/students/request-role`: Gửi yêu cầu lên Admin.
- `POST /api/students/approve-role`: Admin phê duyệt và cấp mã SV.
- `POST /api/students`: Sinh viên đăng ký tham gia một lớp học.
- `GET /api/students/my-classes`: Xem danh sách lớp và điểm của bản thân.
- `PUT /api/students/update-grade`: Cập nhật điểm (Dành cho giảng viên).
- `POST /api/students/{id}/upload-avatar`: Tải lên tệp tin (Lỗi RCE).

### C. Module Lớp học & Giảng viên (Class & Teacher)
- `POST /api/teachers`: Tạo tài khoản giảng viên mới.
- `POST /api/classes`: Tạo lớp học mới kèm mô tả.
- `GET /api/classes`: Xem toàn bộ danh sách lớp học trong hệ thống.
- `GET /api/classes/by-teacher`: Xem danh sách lớp của một giảng viên cụ thể.

---

## 6. Danh mục Lỗ hổng bảo mật (Vulnerability Map)

| OWASP ID | Tên lỗ hổng | Vị trí xuất hiện |
| :--- | :--- | :--- |
| **A01:2021** | **Broken Access Control** | IDOR tại `my-classes`, `getStudentDetail`. Bypassing Role tại `approve-role`, `update-grade`. |
| **A02:2021** | **Security Misconfiguration** | Lộ Stack Trace SQL khi nhập sai dữ liệu. CORS mở hoàn toàn (`*`). |
| **A03:2021** | **Injection (SQLi)** | Xuất hiện ở **TẤT CẢ** các tham số đầu vào do sử dụng nối chuỗi SQL thủ công. |
| **A04:2021** | **Cryptographic Failures** | Mật khẩu lưu Plaintext. Token chỉ là Base64 của email (không có chữ ký số). |
| **A05:2021** | **Security Logging Failures** | Hệ thống hoàn toàn không ghi log các hành vi nhạy cảm. |
| **A08:2021** | **Software & Data Integrity** | **Unrestricted File Upload**: Cho phép upload script `.jsp` thực thi lệnh shell. |

---
*Tài liệu này được tạo ra để phục vụ mục đích học tập. Vui lòng không sử dụng mã nguồn này cho các hệ thống thực tế.*
