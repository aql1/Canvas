import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Stack;

public class Canvas {
    private static DrawingPanel drawingPanel;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    // Main GUI setup: Initializes the main application window and the drawing panel.
    private static void createAndShowGUI() {

        JFrame frame = new JFrame("Graphics Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1680, 1000);

        drawingPanel = new DrawingPanel();
        frame.add(drawingPanel, BorderLayout.CENTER);

        MainPanel mainPanel = new MainPanel(drawingPanel);
        frame.add(mainPanel, BorderLayout.NORTH);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    /**
     * The DrawingPanel class represents a JPanel that provides drawing 
     * and painting capabilities. 
     * It includes features such as freehand drawing, erasing, undo and 
     * redo functionality, 
     * changing the pen settings (color and thickness), adding text, and 
     * clearing the drawing area.
     * It extends JPanel and incorporates various drawing and interaction 
     * methods.
     * It also maintains undo and redo stacks to facilitate non-destructive 
     * editing of drawings and provides tools for controlling the drawing attributes.
     */
static class DrawingPanel extends JPanel {

        private int prevX, prevY;
        private Color currentColor = Color.BLACK;
        private int currentThickness = 2;
        private boolean isDrawing = false;
        private boolean isErasing = false;
        private static final Color ERASER_COLOR = Color.WHITE;
        private BufferedImage bufferImage;
        private Graphics2D bufferGraphics;
        private Stack<BufferedImage> undoStack;
        private Stack<BufferedImage> redoStack;

        public DrawingPanel() {

            setBackground(Color.WHITE);
            undoStack = new Stack<>();
            redoStack = new Stack<>();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    prevX = e.getX();
                    prevY = e.getY();
                    isDrawing = true;
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isDrawing = false;
                    updateUndoRedoStack(bufferImage);
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDrawing || isErasing) {
                        int x = e.getX();
                        int y = e.getY();

                        if (bufferGraphics != null) {
                            if (!isErasing) {
                                bufferGraphics.setColor(currentColor);
                                bufferGraphics.setStroke(new BasicStroke(currentThickness));
                                bufferGraphics.drawLine(prevX, prevY, x, y);
                            } else {
                                bufferGraphics.setColor(ERASER_COLOR);
                                bufferGraphics.fillRect(x - 5, y - 5, 10, 10);
                            }
                        }

                        prevX = x;
                        prevY = y;
                        repaint();
                    }
                }
            });
        }

        public void playAnimationsredo() {
            if (!redoStack.isEmpty()) {
                Timer timer = new Timer(500, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!redoStack.isEmpty()) {
                            redo();
                        } else {
                            ((Timer) e.getSource()).stop();
                        }
                    }
                });
                timer.start();
            }
        }

        public void playAnimationsUndo() {
            if (!undoStack.isEmpty()) {
                Timer timer = new Timer(500, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!undoStack.isEmpty()) {
                            undo();
                        } else {
                            ((Timer) e.getSource()).stop();
                        }
                    }
                });
                timer.start();
            }
        }

        public void setPenSettings(Color color, int thickness) {
            if (isErasing) {
                currentColor = ERASER_COLOR;
            } else {
                currentColor = color;
            }
            currentThickness = thickness;
        }

        public int getCurrentThickness() {
            return currentThickness;
        }

        public Color getCurrentColor() {
            return currentColor;
        }

        public void setCurrentColor(Color color) {
            currentColor = color;
        }

        public void setCurrentThickness(int thickness) {
            currentThickness = thickness;
        }

        public void setEraser(boolean eraserMode) {
            isErasing = eraserMode;
        }

        public void clearPanel() {
            if (bufferGraphics != null) {
                bufferGraphics.setColor(Color.WHITE);
                bufferGraphics.fillRect(0, 0, getWidth(), getHeight());
                repaint();
            }
        }

        public void addText(String text, Font font, Point position, Color textColor) {
            if (bufferGraphics != null) {
                bufferGraphics.setFont(font);
                bufferGraphics.setColor(textColor);
                bufferGraphics.drawString(text, position.x, position.y);
                repaint();
            }
        }

        private void updateUndoRedoStack(BufferedImage image) {
            BufferedImage copy = copyImage(image);
            undoStack.push(copy);
            redoStack.clear();
        }

        public void undo() {
            if (!undoStack.isEmpty()) {
                BufferedImage lastImage = undoStack.pop();
                redoStack.push(copyImage(bufferImage));
                bufferGraphics.drawImage(lastImage, 0, 0, this);
                repaint();

            }
            if (undoStack.isEmpty()) {
                redoStack.push(copyImage(bufferImage));

                bufferGraphics.setColor(Color.WHITE);
                bufferGraphics.fillRect(0, 0, getWidth(), getHeight());
                repaint();

            }

        }

        public void redo() {
            if (!redoStack.isEmpty()) {
                BufferedImage lastImage = redoStack.pop();
                undoStack.push(copyImage(bufferImage));
                bufferGraphics.drawImage(lastImage, 0, 0, this);
                repaint();
            }
        }

        private BufferedImage copyImage(BufferedImage source) {
            if (source == null)
                return null;

            BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
            Graphics2D g = copy.createGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();
            return copy;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bufferImage == null) {
                int width = getWidth();
                int height = getHeight();
                bufferImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                bufferGraphics = bufferImage.createGraphics();
                bufferGraphics.setColor(Color.WHITE);
                bufferGraphics.fillRect(0, 0, width, height);
            }
            g.drawImage(bufferImage, 0, 0, this);
        }

        public void drawVector(int x, int y) {
            if (bufferGraphics != null) {
                bufferGraphics.setColor(currentColor);
                bufferGraphics.setStroke(new BasicStroke(currentThickness));
                bufferGraphics.drawLine(0, 0, x, y);
                updateUndoRedoStack(bufferImage);
                repaint();
            }
        }

    }

    /**
     * The MainPanel class represents a JPanel that contains various control buttons and
     * interacts with the DrawingPanel to provide functionalities for a drawing application's
     * user interface. 
     * It includes buttons for selecting pen, eraser, clearing the canvas, working with shapes 
     * and text, undo and redo actions, and saving/opening drawings.
     * This class helps manage the user interface components and actions that enable the
     * user to interact with the drawing area (DrawingPanel) and perform various operations.
     */
static class MainPanel extends JPanel {

        private JButton playAnimationsButtonRedo;

        private JButton playAnimationsButtonUndo;

        private DrawingPanel drawingPanel;

        public MainPanel(DrawingPanel panel) {
            this.drawingPanel = panel;
            setLayout(new FlowLayout(FlowLayout.CENTER));

            JButton penButton = createPenButton();
            add(penButton);

            JButton eraserButton = createEraserButton();
            add(eraserButton);

            JButton clearButton = createClearButton();
            add(clearButton);

            JButton shapesButton = createShapesButton();
            add(shapesButton);

            JButton textButton = createTextButton();
            add(textButton);

            JButton undoButton = createUndoButton();
            add(undoButton);

            JButton redoButton = createRedoButton();
            add(redoButton);

            JButton vectorButton = createVectorButton();
            add(vectorButton);

            playAnimationsButtonRedo = createPlayAnimationsButtonRedo();
            add(playAnimationsButtonRedo);

            playAnimationsButtonUndo = createPlayAnimationsButtonUndo();
            add(playAnimationsButtonUndo);

            JButton openImageButton = createOpenImageButton();
            add(openImageButton);

            JButton saveButton = createSaveButton();
            add(saveButton);
        }

        private JButton createPenButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(80, 60));
            ImageIcon PenIcon = new ImageIcon("Pen.png");
            button.setIcon(PenIcon);
            PenButton penButton = new PenButton(drawingPanel);
            button.addActionListener(e -> {
                penButton.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                penButton.setVisible(true);
            });
            return button;
        }

        private JButton createClearButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(80, 60));
            ImageIcon ClearIcon = new ImageIcon("Clear.png");
            button.setIcon(ClearIcon);
            button.addActionListener(e -> drawingPanel.clearPanel());
            return button;
        }

        private JButton createEraserButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(80, 60));
            ImageIcon EraserIcon = new ImageIcon("Eraser.png");
            button.setIcon(EraserIcon);
            button.addActionListener(e -> toggleEraser());
            return button;
        }

        private void toggleEraser() {
            drawingPanel.setEraser(!drawingPanel.isErasing);
        }

        private JButton createShapesButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(80, 60));
            ImageIcon ShapesIcon = new ImageIcon("Shapes.png");
            button.setIcon(ShapesIcon);
            button.addActionListener(e -> showShapesButton());
            return button;
        }

        private void showShapesButton() {
            ShapesButton dialog = new ShapesButton(this, drawingPanel.bufferGraphics);
            dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            dialog.setSize(600, 800);
            dialog.setVisible(true);
        }

        private JButton createTextButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(80, 60));
            ImageIcon TextsIcon = new ImageIcon("Text.png");
            button.setIcon(TextsIcon);
            button.addActionListener(e -> showTextButton());
            return button;
        }

        private void showTextButton() {
            TextButton dialog = new TextButton(drawingPanel);
            dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        }

        private JButton createUndoButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(80, 60));
            ImageIcon UndoIcon = new ImageIcon("undo.png");
            button.setIcon(UndoIcon);
            button.addActionListener(e -> drawingPanel.undo());
            return button;
        }

        private JButton createRedoButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(80, 60));
            ImageIcon DoIcon = new ImageIcon("Do.png");
            button.setIcon(DoIcon);
            button.addActionListener(e -> drawingPanel.redo());
            return button;
        }

        private JButton createVectorButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(80, 60));
            ImageIcon VectorIcon = new ImageIcon("Vector.png");
            button.setIcon(VectorIcon);
            button.addActionListener(e -> showVectorDialog());
            return button;
        }

        private void showVectorDialog() {
            VectorDialog dialog = new VectorDialog(drawingPanel);
            dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        }

        private JButton createPlayAnimationsButtonRedo() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(80, 60));
            ImageIcon RAnimationIcon = new ImageIcon("RAnimation.png");
            button.setIcon(RAnimationIcon);
            button.addActionListener(e -> drawingPanel.playAnimationsredo());
            return button;
        }

        private JButton createPlayAnimationsButtonUndo() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(80, 60));
            ImageIcon UAnimationsIcon = new ImageIcon("UAnimations.png");
            button.setIcon(UAnimationsIcon);
            button.addActionListener(e -> drawingPanel.playAnimationsUndo());
            return button;
        }

        private JButton createSaveButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(80, 60));
            ImageIcon SaveIcon = new ImageIcon("Save.png");
            button.setIcon(SaveIcon);
            button.addActionListener(e -> saveDrawing());
            return button;
        }

        private JButton createOpenImageButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(80, 60));
            ImageIcon OpenIcon = new ImageIcon("Open.png");
            button.setIcon(OpenIcon);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openImage();
                }
            });

            return button;
        }

        private void openImage() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    BufferedImage image = ImageIO.read(selectedFile);
                    if (image != null) {
                        drawingPanel.bufferImage = image;
                        drawingPanel.bufferGraphics = image.createGraphics();
                        drawingPanel.repaint();
                    } else {
                        JOptionPane.showMessageDialog(this, "Unable to open the selected image.", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void saveDrawing() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                if (!selectedFile.getName().toLowerCase().endsWith(".png")) {
                    selectedFile = new File(selectedFile.getAbsolutePath() + ".png");
                }

                try {
                    ImageIO.write(drawingPanel.bufferImage, "png", selectedFile);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * The PenButton class represents a dialog box that allows the user to set pen-related settings,
     * including the pen color and thickness, for the drawing application. This dialog is opened
     * when the user wants to customize the pen properties.
     * This class extends JDialog and provides an interface for the user to select the pen color
     * using a color chooser and adjust the pen's thickness using a slider. Once the user has made
     * their selections, they can click the "Set" button to update the pen settings in the associated
     * DrawingPanel.
     */
static class PenButton extends JDialog {
    
        private DrawingPanel drawingPanel;

        public PenButton(DrawingPanel drawingPanel) {
            this.drawingPanel = drawingPanel;
            setTitle("Pen Settings");
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JLabel colorLabel = new JLabel("Pen Color:");
            JColorChooser colorChooser = new JColorChooser(drawingPanel.getCurrentColor());
            panel.add(colorLabel);
            panel.add(colorChooser);

            JLabel thicknessLabel = new JLabel("Pen Thickness:");
            JSlider thicknessSlider = new JSlider(1, 20, drawingPanel.getCurrentThickness());
            thicknessSlider.setMajorTickSpacing(1);
            thicknessSlider.setPaintTicks(true);
            thicknessSlider.setPaintLabels(true);
            panel.add(thicknessLabel);
            panel.add(thicknessSlider);

            JButton setButton = new JButton("Set");
            setButton.addActionListener(e -> setPenSettings(colorChooser.getColor(), thicknessSlider.getValue()));
            panel.add(setButton);

            add(panel);
            pack();
        }

        /**
         * Sets the pen settings in the associated DrawingPanel with the selected color and thickness,
         * and then closes the dialog.
         */
        private void setPenSettings(Color color, int thickness) {
            drawingPanel.setPenSettings(color, thickness);
            dispose();
        }
    }

    /**
     * The TextButton class represents a dialog box that allows the user to add text to the drawing.
     * Users can customize the text's content, font, size, style (bold and italic), and text color.
     * This dialog is opened when the user wants to insert text into the DrawingPanel.
     *
     * This class extends JDialog and provides an interface for the user to input text, choose a font,
     * set the font size, and apply text styling (bold and italic). Additionally, it offers a color
     * chooser for selecting the text color. When the user clicks "Add Text," the specified text, font,
     * and other settings are applied to the DrawingPanel.
     */    
static class TextButton extends JDialog {
        private JTextArea textArea;
        private JComboBox<String> fontComboBox;
        private JComboBox<Integer> fontSizeComboBox;
        private JCheckBox boldCheckBox;
        private JCheckBox italicCheckBox;
        private JButton addTextButton;
        private JColorChooser textColorChooser;
        private DrawingPanel drawingPanel;

        public TextButton(DrawingPanel drawingPanel) {
            this.drawingPanel = drawingPanel;
            setTitle("Add Text");
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JLabel textColorLabel = new JLabel("Text Color:");
            textColorChooser = new JColorChooser(Color.BLACK);
            panel.add(textColorLabel);
            panel.add(textColorChooser);

            JLabel textLabel = new JLabel("Text:");
            textArea = new JTextArea(4, 20);
            JScrollPane scrollPane = new JScrollPane(textArea);
            panel.add(textLabel);
            panel.add(scrollPane);

            JLabel fontLabel = new JLabel("Font:");
            fontComboBox = new JComboBox<>(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
            panel.add(fontLabel);
            panel.add(fontComboBox);

            JLabel fontSizeLabel = new JLabel("Font Size:");
            fontSizeComboBox = new JComboBox<>(new Integer[] { 12, 16, 20, 24, 28 });
            panel.add(fontSizeLabel);
            panel.add(fontSizeComboBox);

            boldCheckBox = new JCheckBox("Bold");
            panel.add(boldCheckBox);

            italicCheckBox = new JCheckBox("Italic");
            panel.add(italicCheckBox);

            addTextButton = new JButton("Add Text");
            panel.add(addTextButton);

            addTextButton.addActionListener(e -> addTextToDrawing());

            add(panel);
            pack();
        }

        /**
         * Adds the specified text to the DrawingPanel with the chosen font, size, style, position,
         * and text color. After applying the text, the dialog is closed.
         */
        private void addTextToDrawing() {
            String text = textArea.getText();
            String fontName = (String) fontComboBox.getSelectedItem();
            int fontSize = (int) fontSizeComboBox.getSelectedItem();
            int fontStyle = Font.PLAIN;

            if (boldCheckBox.isSelected()) {
                fontStyle += Font.BOLD;
            }

            if (italicCheckBox.isSelected()) {
                fontStyle += Font.ITALIC;
            }

            Font font = new Font(fontName, fontStyle, fontSize);
            Point position = new Point(50, 50);

            Color textColor = textColorChooser.getColor();
            drawingPanel.addText(text, font, position, textColor);
            dispose();
        }
    }

    /**
     * The ShapesButton class represents a dialog box that allows the user to draw various shapes
     * (lines, circles, rectangles, and polygons) on the DrawingPanel. Users can customize the shapes'
     * appearance, such as color, thickness, size, number of vertices, fill option, flipping, and rotation.
     * This dialog provides an interface for users to select different shape parameters and then draw
     * the selected shape on the DrawingPanel.
     *
     * This class extends JDialog and offers options for specifying the shape's characteristics, including
     * color, thickness, size, vertices, fill, flip, and rotation. Users can choose a shape, configure its
     * properties, and then click "Draw" to add the customized shape to the DrawingPanel.
     */
static class ShapesButton extends JDialog {
        private JComboBox<String> shapeComboBox;
        private JComboBox<Integer> sizeComboBox;
        private JComboBox<Integer> verticesComboBox;
        private JCheckBox fillCheckBox;
        private JCheckBox flipCheckBox;
        private JRadioButton horizontalFlipRadioButton;
        private JRadioButton verticalFlipRadioButton;
        private JCheckBox rotateCheckBox;
        private JFormattedTextField angleField;
        private JButton drawButton;
        private MainPanel mainPanel;
        private Graphics2D bufferGraphics;
        private JColorChooser colorChooser;
        private JSlider thicknessSlider;

        public ShapesButton(MainPanel mainPanel, Graphics2D bufferGraphics) {
            this.mainPanel = mainPanel;
            this.bufferGraphics = bufferGraphics;
            setTitle("Shapes Dialog");
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JLabel colorLabel = new JLabel("Color:");
            colorChooser = new JColorChooser(Color.BLACK);
            panel.add(colorLabel);
            panel.add(colorChooser);

            JLabel thicknessLabel = new JLabel("Thickness:");
            thicknessSlider = new JSlider(1, 20, mainPanel.drawingPanel.getCurrentThickness());
            thicknessSlider.setMajorTickSpacing(1);
            thicknessSlider.setPaintTicks(true);
            thicknessSlider.setPaintLabels(true);
            panel.add(thicknessLabel);
            panel.add(thicknessSlider);

            JLabel shapeLabel = new JLabel("Select Shape:");
            shapeComboBox = new JComboBox<>(new String[] { "Line", "Circle", "Rectangle", "Polygon" });
            panel.add(shapeLabel);
            panel.add(shapeComboBox);

            JLabel sizeLabel = new JLabel("Select Size:");
            sizeComboBox = new JComboBox<>(new Integer[] { 20, 30, 40, 50 });
            panel.add(sizeLabel);
            panel.add(sizeComboBox);

            JLabel verticesLabel = new JLabel("Num of Vertices:");
            verticesComboBox = new JComboBox<>(new Integer[] { 3, 4, 5, 6, 7, 8, 9, 10 });
            panel.add(verticesLabel);
            panel.add(verticesComboBox);

            fillCheckBox = new JCheckBox("Fill Shape");
            panel.add(fillCheckBox);

            flipCheckBox = new JCheckBox("Flip Shape");
            panel.add(flipCheckBox);

            horizontalFlipRadioButton = new JRadioButton("Horizontal Flip");
            verticalFlipRadioButton = new JRadioButton("Vertical Flip");

            ButtonGroup flipGroup = new ButtonGroup();
            flipGroup.add(horizontalFlipRadioButton);
            flipGroup.add(verticalFlipRadioButton);

            panel.add(horizontalFlipRadioButton);
            panel.add(verticalFlipRadioButton);

            rotateCheckBox = new JCheckBox("Rotate Shape");
            panel.add(rotateCheckBox);

            JLabel angleLabel = new JLabel("Rotation Angle (degrees):");
            NumberFormat format = NumberFormat.getNumberInstance();
            angleField = new JFormattedTextField(format);
            angleField.setColumns(4);
            angleField.setValue(0); // Default angle
            panel.add(angleLabel);
            panel.add(angleField);

            drawButton = new JButton("Draw");
            panel.add(drawButton);

            drawButton.addActionListener(e -> drawShape());

            add(panel);
            pack();
        }

        /**
         * Draws the selected shape on the DrawingPanel based on the user's customization of shape properties,
         * including color, thickness, size, vertices, fill option, flipping, and rotation.
         */
        private void drawShape() {
            String selectedShape = (String) shapeComboBox.getSelectedItem();
            int selectedSize = (int) sizeComboBox.getSelectedItem();
            int numVertices = (int) verticesComboBox.getSelectedItem();
            boolean fillShape = fillCheckBox.isSelected();
            boolean flipShape = flipCheckBox.isSelected();
            boolean rotateShape = rotateCheckBox.isSelected();

            Color drawColor = colorChooser.getColor();
            int thickness = thicknessSlider.getValue();

            int centerX = (getWidth() / 2)+500;
            int centerY = (getHeight() / 2);

            AffineTransform originalTransform = bufferGraphics.getTransform();

            if (rotateShape) {
                double rotationAngle = Double.parseDouble(angleField.getText());

                int x = centerX; 
                int y = centerY; 

                bufferGraphics.translate(x, y);

                bufferGraphics.rotate(Math.toRadians(rotationAngle));

                bufferGraphics.translate(-x, -y);
            }

            if (flipShape) {
                bufferGraphics.translate(centerX, centerY);

                if (horizontalFlipRadioButton.isSelected()) {
                    bufferGraphics.scale(-1, 1); 
                } else if (verticalFlipRadioButton.isSelected()) {
                    bufferGraphics.scale(1, -1); 
                }

                bufferGraphics.translate(-centerX, -centerY);
            }

            bufferGraphics.setColor(drawColor);
            bufferGraphics.setStroke(new BasicStroke(thickness));

            if (selectedShape.equals("Line")) {
                int x1 = centerX - selectedSize * 5;
                int y1 = centerY - selectedSize * 5;
                int x2 = centerX + selectedSize * 5;
                int y2 = centerY + selectedSize * 5;
                bufferGraphics.drawLine(x1, y1, x2, y2);
            } else if (selectedShape.equals("Circle")) {
                int circleSize = (int) (selectedSize * 10);
                int x = centerX - circleSize / 2;
                int y = centerY - circleSize / 2;
                if (fillShape) {
                    bufferGraphics.fillOval(x, y, circleSize, circleSize);
                } else {
                    bufferGraphics.drawOval(x, y, circleSize, circleSize);
                }
            } else if (selectedShape.equals("Rectangle")) {
                int rectangleWidth = (int) (selectedSize * 20); 
                int rectangleHeight = (int) (selectedSize * 10); 
                int x = centerX - rectangleWidth / 2;
                int y = centerY - rectangleHeight / 2;
                if (fillShape) {
                    bufferGraphics.fillRect(x, y, rectangleWidth, rectangleHeight);
                } else {
                    bufferGraphics.drawRect(x, y, rectangleWidth, rectangleHeight);
                }
            } else if (selectedShape.equals("Polygon")) {
                int radius = (int) (selectedSize * 5);
                int[] xPoints = new int[numVertices];
                int[] yPoints = new int[numVertices];
                for (int i = 0; i < numVertices; i++) {
                    double angle = 2 * Math.PI * i / numVertices;
                    xPoints[i] = (int) (centerX + radius * Math.cos(angle));
                    yPoints[i] = (int) (centerY + radius * Math.sin(angle));
                }
                if (fillShape) {
                    bufferGraphics.fillPolygon(xPoints, yPoints, numVertices);
                } else {
                    bufferGraphics.drawPolygon(xPoints, yPoints, numVertices);
                }
            }

            bufferGraphics.setTransform(originalTransform);

            mainPanel.drawingPanel.repaint();
        }
    }

    /**
     * The VectorDialog class represents a dialog box for drawing a vector line on the DrawingPanel.
     * Users can specify the X and Y coordinates for the vector's endpoint within the specified range.
     * This dialog provides an interface for users to enter coordinates and then draw a vector line on
     * the DrawingPanel by clicking the "Draw" button.
     */    
static class VectorDialog extends JDialog {
        private JTextField xCoordinateField;
        private JTextField yCoordinateField;
        private JButton drawButton;
        private DrawingPanel drawingPanel;

        /**
     * Constructs a VectorDialog with the specified DrawingPanel.
     *
     * drawingPanel is the DrawingPanel on which the vector line will be drawn.
     */
        public VectorDialog(DrawingPanel drawingPanel) {
            this.drawingPanel = drawingPanel;
            setTitle("Vector Line");
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JLabel xLabel = new JLabel("X Coordinate: (0-1920)");
            xCoordinateField = new JTextField(10);
            panel.add(xLabel);
            panel.add(xCoordinateField);

            JLabel yLabel = new JLabel("Y Coordinate: (0-1050)");
            yCoordinateField = new JTextField(10);
            panel.add(yLabel);
            panel.add(yCoordinateField);

            drawButton = new JButton("Draw");
            panel.add(drawButton);

            drawButton.addActionListener(e -> drawVector());

            add(panel);
            pack();
        }

        /**
         * Draws a vector line on the DrawingPanel based on the user's input coordinates.
         * The method validates the coordinates and displays an error message if they are outside the
         * specified range. If the coordinates are valid, the vector line is drawn on the DrawingPanel.
         */
        private void drawVector() {
            try {
                int x = Integer.parseInt(xCoordinateField.getText());
                int y = Integer.parseInt(yCoordinateField.getText());

                if (x < 0 || x > 1920 || y < 0 || y > 1050) {
                    JOptionPane.showMessageDialog(this,
                            "X and Y coordinates must be within the specified range.\nX: 0-1920, Y: 0-1050",
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    drawingPanel.drawVector(x, y);
                    dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid coordinates.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}