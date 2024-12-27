# Graphics Editor Application

This is a Java-based **Graphics Editor** application that provides a versatile and interactive drawing environment. The application supports freehand drawing, text addition, shape drawing, undo/redo functionality, animations, and file management (save/open images).

---

## Features

- **Drawing Tools**:
  - Freehand drawing with customizable pen settings (color, thickness).
  - Eraser tool for precise edits.
- **Shape Tools**:
  - Draw various shapes (lines, circles, rectangles, polygons) with options for size, vertices, color, fill, flipping, and rotation.
- **Text Tool**:
  - Add text with custom font, size, style (bold/italic), and color.
- **Undo/Redo**:
  - Unlimited undo/redo operations with animation support.
- **File Management**:
  - Save drawings as PNG images.
  - Open existing PNG images for editing.
- **Customizations**:
  - Flip shapes horizontally or vertically.
  - Rotate shapes by a specified angle.

---

## Requirements

- Java Development Kit (JDK) 8 or higher

---

## How to Run the Application

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-username/GraphicsEditor.git
   cd GraphicsEditor
   ```

2. **Compile the Code:**
   Compile the application using `javac`:
   ```bash
   javac Canvas.java
   ```

3. **Run the Application:**
   Execute the application with `java`:
   ```bash
   java Canvas
   ```

---

## Usage

### Drawing Panel
- Interact with the drawing area to create drawings or add elements.
- Use the mouse for freehand drawing or dragging shapes.

### Toolbar Buttons

1. **Pen Tool**:
   - Opens a dialog to customize pen settings (color, thickness).

2. **Eraser Tool**:
   - Activates the eraser mode to remove parts of the drawing.

3. **Shapes Tool**:
   - Opens a dialog for selecting and customizing shapes (color, size, vertices, rotation, etc.).

4. **Text Tool**:
   - Opens a dialog for adding text to the canvas with custom font settings.

5. **Undo/Redo**:
   - Undo or redo the last action.
   - Supports animated playback of undo/redo actions.

6. **Save/Open Buttons**:
   - Save your drawing as a PNG image.
   - Open and edit an existing PNG image.

7. **Clear Button**:
   - Clears the entire drawing area.

---

## File Structure

```
GraphicsEditor/
├── Canvas.java     # Main application file
├── assets/         # Image assets for buttons (e.g., icons)
└── README.md       # Project documentation (this file)
```

---

## Notes

- Ensure the `assets/` folder contains all necessary icons (e.g., Pen.png, Eraser.png).
- The application initializes with a blank canvas.
- Custom animations are supported for undo/redo actions.

---

## Example Interaction

### Drawing a Shape
1. Click on the **Shapes Tool**.
2. Select "Circle" from the shape dropdown.
3. Choose size, fill option, and color.
4. Click "Draw" to add the shape to the canvas.

### Adding Text
1. Click on the **Text Tool**.
2. Enter text, select font, size, and color.
3. Click "Add Text" to place it on the canvas.

---
