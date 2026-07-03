# Construction Legal Lookup

Hệ thống tra cứu văn bản pháp luật ngành xây dựng tích hợp AI.

## Tính năng chính

- 🔍 **Tra cứu văn bản**: Full-text search, bộ lọc theo loại, cơ quan ban hành, năm, trạng thái hiệu lực
- 📚 **Xem chi tiết**: Nội dung, mục lục điều khoản, liên kết đến PDF
- 💾 **Bookmark & lịch sử**: Lưu văn bản yêu thích, xem lịch sử tìm kiếm/xem
- 🤖 **AI hỗ trợ**: Tóm tắt, hỏi đáp, giải thích nội dung (Gemini)
- 📊 **Admin dashboard**: Quản lý văn bản, user, thống kê hoạt động
- 🔗 **Quan hệ văn bản**: Hiển thị các văn bản liên quan, được hướng dẫn, sửa đổi, thay thế

## Công nghệ sử dụng

- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Database**: MySQL 8.x (Flyway migrations)
- **Cache & Rate Limiting**: Redis 7.x
- **Tích hợp AI**: Google Gemini
- **Cloud Storage**: Cloudinary (PDF)
- **Containerization**: Docker, Docker Compose

## Bắt đầu

### Yêu cầu

- Java 17+
- Maven 3.9+
- Docker & Docker Compose (để chạy MySQL & Redis)

### Cài đặt & chạy

1. **Clone repo**:
   ```bash
   git clone <repo-url>
   cd ConstructionLegalLookup
   ```

2. **Khởi động MySQL & Redis**:
   ```bash
   docker-compose up -d
   ```

3. **Cấu hình môi trường** (nếu cần):
   - Copy `construction-legal-lookup-app/src/main/resources/application-dev.yaml.example` (nếu có) và cập nhật cấu hình
   - API key cho Gemini và Cloudinary được đặt trong `application-dev.yaml`

4. **Chạy ứng dụng**:
   ```bash
   cd construction-legal-lookup-app
   mvn spring-boot:run
   ```

5. **Truy cập**:
   - API: `http://localhost:8080/api`
   - Swagger UI: `http://localhost:8080/api/swagger-ui/index.html`

## Tài khoản demo

| Email              | Mật khẩu   | Vai trò |
|--------------------|------------|---------|
| user@demo.com      | Demo@123   | User    |
| admin@demo.com     | Admin@123  | Admin   |

## Tài liệu thêm

- [API Documentation](docs/API.md)
- [Database Design](docs/DATABASE.md)

## Đóng góp

Mọi đóng góp đều được chào đón!

## License

MIT
