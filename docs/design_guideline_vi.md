# MDT UI Framework - Hướng dẫn thiết kế (Design Guideline)

Tài liệu này xác định các tiêu chuẩn trực quan, token thiết kế, mô hình bố cục phản hồi (responsive layout patterns) và các hướng dẫn tương tác vi mô (micro-interactions) để xây dựng các giao diện nhất quán, hiện đại và đẹp mắt bằng cách sử dụng MDT UI Framework.

---

## 1. Tính thẩm mỹ trực quan & Token thiết kế

Để đạt được giao diện cao cấp, hiện đại (tránh các thiết kế thô sơ hoặc mặc định), hãy tuân thủ các token thiết kế sau:

### 1. Bảng màu (Color Palettes)
Tránh sử dụng các màu cơ bản quá bão hòa (đỏ, xanh lá, xanh lam thuần túy). Hãy sử dụng mã hex được tinh tuyển cho giao diện tối:
* **Màu nền (Backgrounds)**: Các tông màu đen huyền bí / xanh thẫm:
  - Bảng điều khiển chính (Base Panel): `#1c1c22`
  - Thẻ thiết lập bên trong (Settings Card): `#303052`
  - Trạng thái hover tương tác: `#55556a`
* **Màu nhấn / Trạng thái (Accents / Status)**:
  - Thành công (Success - Xanh lá): `#5cb85c`
  - Thông tin (Info - Xanh dương): `#5bc0de`
  - Cảnh báo (Warning - Vàng): `#f0c040`
  - Nguy hiểm (Danger - Đỏ): `#d9534f`
  - Màu nhấn chính (Primary Pink): `#ff79c6`

### 2. Thiết kế kính mờ (Glassmorphism) & Backdrop Filters
Đối với các menu phủ, hộp thoại (dialogs) và popups, hãy sử dụng phong cách kính mờ:
- **Độ mờ (Opacity)**: Sử dụng từ `0.8f` đến `0.9f` để tạo độ trong suốt vừa phải.
- **Đường viền (Borders)**: Thêm viền mỏng màu trắng nửa trong suốt (`border(1f, Color.valueOf("ffffff15"))`) để đóng khung phần tử.
- **Bo góc (Radii)**: Sử dụng các góc bo mềm mại (thường là `12f` cho bảng điều khiển, `8f` cho các khối cài đặt, `6f` cho các nút bấm).

---

## 2. Bố cục & Phân cấp trực quan

Luôn luôn cấu trúc các container UI bằng cách chia lớp rõ ràng để thiết lập hệ thống phân cấp trực quan:

```
+-------------------------------------------------------+
|  Layout (Root Panel - Khung gốc)                      |
|  +-------------------------------------------------+  |
|  |  Layout (Header Section - Phần đầu)              |  |
|  |  [Text: Preview Demo]                            |  |
|  +-------------------------------------------------+  |
|  +------------------------+ +----------------------+  |
|  |  Layout (Sidebar)      | |  CustomComponent     |  |
|  |  - Cài đặt viền        | |  (Reactive Preview)  |  |
|  |  - Chọn màu sắc        | |                      |  |
|  |  - Thanh độ mờ         | |                      |  |
|  |                        | |                      |  |
|  +------------------------+ +----------------------+  |
+-------------------------------------------------------+
```

### 1. Khoảng cách & Lề
- **Padding của Container**: Luôn áp dụng padding tiêu chuẩn là `16f` cho các container chính.
- **Khoảng cách giữa các phần tử (Gap)**: Sử dụng gap là `8f` khi liệt kê các tùy chọn theo chiều dọc hoặc chiều ngang.
- **Khoảng trống (Spacers)**: Tránh để các nhóm layout trống trừ khi được tạo ra làm spacer (ví dụ: `CustomComponent.of().style(s -> s.opacity(0f).fixedHeight(8f))`).

### 2. Ràng buộc kích thước (Sizing Constraints)
- Tránh việc gán cứng tọa độ tuyệt đối cho các phần tử cần co giãn. Thay vào đó, hãy tận dụng `SizeMode.GROW` kết hợp với các ràng buộc:
  - Cho thanh công cụ / cài đặt: sử dụng chiều rộng cố định (ví dụ: `fixedWidth(260f)`) và để chiều cao tự động co giãn hoặc bao bọc nội dung.
  - Cho các khối xem trước động (preview blocks): sử dụng `grow()` để chiếm toàn bộ không gian còn lại.

---

## 3. Tương tác vi mô & Phản hồi (Micro-Interactions)

Các layout tĩnh sẽ cho cảm giác thiếu sức sống. Hãy bổ sung phản hồi khi người dùng tương tác:

### 1. Trạng thái Hover (Liên kết trạng thái Reactive)
Để giữ cấu trúc dạng cây khai báo và lồng nhau của các component (không cần lưu trữ tham chiếu vào biến cục bộ), hãy luôn liên kết các style hover một cách reactive thông qua trạng thái `Signal<Boolean>` thay vì thay đổi style một cách thủ công (imperative) trong các hàm callback:

```java
Signal<Boolean> isHovered = new Signal<>(false);

CustomComponent.of()
    .style(s -> s.background(isHovered.get() ? hoverColor : normalColor))
    .onHover(() -> isHovered.set(true))
    .onExit(() -> isHovered.set(false));
```
Cách này giúp cây phân cảnh (scene graph) luôn lồng ghép gọn gàng, fluent và cập nhật phản xạ tự động.

### 2. Trạng thái Active (Kích hoạt)
Cung cấp phản hồi trực quan tức thì khi nhấp chuột (ví dụ: đổi màu nền, nhấp nháy viền nhẹ, hoặc cập nhật văn bản phản hồi reactive).

---

## 4. Tỷ lệ cỡ chữ (Typography Scale)

Luôn luôn sử dụng font **JetBrains Mono** thông qua `FontManager`. Đảm bảo quy tắc cỡ chữ nhất quán:
- **Tiêu đề lớn / Header**: Cỡ chữ `1.4f` (hỗ trợ màu markup).
- **Nhãn mô tả / Labels**: Cỡ chữ `0.8f` (màu chữ tối hơn, mô tả chi tiết).
- **Giá trị hiển thị / Text nút bấm**: Cỡ chữ `0.9f` (màu trắng, độ tương phản cao).
