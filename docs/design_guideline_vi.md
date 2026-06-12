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
|  LayoutWidget (Root Panel - Khung gốc)                |
|  +-------------------------------------------------+  |
|  |  LayoutWidget (Header Section - Phần đầu)       |  |
|  |  [TextWidget: Preview Demo]                     |  |
|  +-------------------------------------------------+  |
|  +------------------------+ +----------------------+  |
|  |  LayoutWidget (Sidebar)| |  CustomWidget        |  |
|  |  - Cài đặt viền        | |  (Reactive Preview)  |  |
|  |  - Chọn màu sắc        | |                      |  |
|  |  - Thanh độ mờ         | |                      |  |
|  |                        | |                      |  |
|  +------------------------+ +----------------------+  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

### 1. Khoảng cách & Lề
- **Padding của Container**: Luôn áp dụng padding tiêu chuẩn là `12f` đến `16f` cho các container chính.
- **Khoảng cách giữa các phần tử (Gap)**: Sử dụng gap là `8f` đến `12f` khi liệt kê các tùy chọn theo chiều dọc hoặc chiều ngang.
- **Khoảng trống (Spacers)**: Tránh để các nhóm layout trống. Nếu cần khoảng trống cố định, hãy dùng widget cấu hình rỗng:
  ```java
  LayoutWidget.builder().fixedHeight(8f).build()
  ```

### 2. Ràng buộc kích thước (Sizing Constraints)
- Tránh việc gán cứng tọa độ tuyệt đối cho các phần tử cần co giãn. Thay vào đó, hãy tận dụng `NodeSpec.SizeMode.GROW` kết hợp với các cấu hình:
  - Cho thanh công cụ / cài đặt: sử dụng chiều rộng cố định (ví dụ: `fixedWidth(320f)`) và để chiều cao tự động co giãn.
  - Cho các khối xem trước động (preview blocks): sử dụng `widthMode(NodeSpec.SizeMode.GROW).heightMode(NodeSpec.SizeMode.GROW)` để chiếm toàn bộ không gian còn lại.

---

## 3. Tương tác vi mô & Phản hồi trạng thái (State-driven Styling)

Để giữ cấu trúc dạng cây khai báo và lồng nhau của các widget bất biến, tất cả các thay đổi giao diện phải được thúc đẩy bởi dữ liệu trạng thái (`Signal<AppState>`).

### Trạng thái Active (Kích hoạt)
Thay vì sửa đổi style trực tiếp bằng mã imperative, hãy tính toán các thuộc tính màu sắc, đường viền dựa trên biến trạng thái của tab hiện tại:

```java
boolean isActive = s.activeTab() == tabIndex;
Color bg = isActive ? Color.valueOf("ff79c6") : Color.valueOf("303042");

return LayoutWidget.builder()
    .background(CustomWidget.builder().fillColor(bg).build())
    .onClick(() -> state.set(state.get().withActiveTab(tabIndex)))
    .children(...)
    .build();
```
Khi click được kích hoạt, giá trị signal thay đổi sẽ phát hoạt lại root rebuild, vẽ lại các widget tương thích tự động qua cơ chế đối chiếu.

---

## 4. Tỷ lệ cỡ chữ (Typography Scale)

Đảm bảo quy tắc cỡ chữ nhất quán khi sử dụng `TextWidget`:
- **Tiêu đề lớn / Header**: Cỡ chữ `1.3f` (hỗ trợ màu markup của Mindustry như `[ff79c6]`).
- **Nhãn mô tả / Labels**: Cỡ chữ `0.8f` (màu chữ tối hơn như `Color.lightGray`).
- **Giá trị hiển thị / Text nút bấm**: Cỡ chữ `0.9f` (màu trắng, độ tương phản cao).
