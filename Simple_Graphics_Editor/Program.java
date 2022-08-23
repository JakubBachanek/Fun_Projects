/**
 * Simple graphics editor
 *
 * @author Jakub Bachanek
 * @version 1.0
 *
 */


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.*;
import javax.swing.*;
import java.lang.*;
import java.util.ArrayList;


class MyWindowAdapter extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }
}


/*--------------------------------------------------------------------*/



class EditorFrame extends JFrame {
    int currentClicked = 1;
    Color currentColor = Color.BLACK;
    int currentSelectedShape = -1;
    ArrayList<Point> points = new ArrayList<Point>();
    ArrayList<ColoredShape> shapes = new ArrayList<ColoredShape>();

    ColoredShape scaleShapes[] = new ColoredShape[2];
    {
    scaleShapes[0] = new ColoredShape(null, null);
    scaleShapes[1] = new ColoredShape(null, null);
    }

    int startScaleX;
    int startScaleY;

    JPanel panelMain = new Surface();
    JPanel panelBottom = new JPanel();

    String stringDimensions = new String("Dimensions: ");
    String stringCoordinates = new String("Coordinates: ");

    JLabel labelSize = new JLabel();
    JLabel labelCoordinates = new JLabel();

    MyMenu myMenu = new MyMenu();
    

    /**
     *
     *  This class consists of classes which control mouse actions on panel
     *  and methods which create and draw geometric figures
     *
     */

    class Surface extends JPanel {
        Point startDrag, endDrag, middlePoint;

        public Surface() {
            addMouseListener(new PressMouse());
            addMouseMotionListener(new DragMouse());
        }

        /**
         *
         *  This class contains mousePressed and mouseReleased methods
         *
         */
        class PressMouse extends MouseAdapter {
            @Override
            /**
             *  Method which tracks the id of selected shape
             *  and coordinates of pressed mouse
             */
            public void mousePressed(MouseEvent e) {
                startDrag = new Point(e.getX(), e.getY());
                endDrag = startDrag;

                if(currentClicked == 4 || currentClicked == 5) {
                    for(int index = shapes.size(), temp = 0; index > 0 && temp == 0; index--) {
                        if((shapes.get(index - 1).isHit(endDrag.x, endDrag.y) & scaleShapes[1].shape != null && !scaleShapes[1].isHit(endDrag.x, endDrag.y)) || ((shapes.get(index - 1).isHit(endDrag.x, endDrag.y) & scaleShapes[1].shape == null))) {
                            currentSelectedShape = index - 1;
                            temp = 1;
                            repaint();
                        }
                    }
                    changeLabel();
                }

                if(currentClicked == 5 && currentSelectedShape >= 0 && shapes.get(currentSelectedShape).isHit(endDrag.x, endDrag.y)) {
                    scaleShapes[0].shape = shapes.get(currentSelectedShape).shape;
                    scaleShapes[1].shape = createScaleRectangle(shapes.get(currentSelectedShape).shape);
                    startScaleX = (int) scaleShapes[1].shape.getBounds().getX();
                    startScaleY = (int) scaleShapes[1].shape.getBounds().getY();
                }

                int deltaX = endDrag.x - startDrag.x;
                int deltaY = endDrag.y - startDrag.y;

                middlePoint = new Point(startDrag.x + deltaX, startDrag.y + deltaY);
                repaint();
            }


            /**
             *  This method creates new shapes and adds them to ArrayList
             */
            public void mouseReleased(MouseEvent e) {
                ColoredShape newColoredShape = new ColoredShape(null, null) ;
                
                if (e.getButton() == MouseEvent.BUTTON3 && currentClicked == 3) {
                    points.add(new Point(e.getX(), e.getY()));
                    newColoredShape.shape = makePolygon(points, e.getX(), e.getY());
                    newColoredShape.color = currentColor;
                    shapes.add(newColoredShape);
                    currentSelectedShape = shapes.size() - 1;
                    points.clear();
                } else if(currentClicked == 1 && Math.abs(startDrag.x - e.getX()) > 0 && Math.abs(startDrag.y - e.getY()) > 0) {
                    newColoredShape.shape = makeRectangle(startDrag.x, startDrag.y, e.getX(), e.getY());
                    newColoredShape.color = currentColor;
                    shapes.add(newColoredShape);
                    currentSelectedShape = shapes.size() - 1;
                } else if(currentClicked == 2 && Math.abs(startDrag.x - e.getX()) > 0 && Math.abs(startDrag.y - e.getY()) > 0) {
                    newColoredShape.shape = makeEllipse(startDrag.x, startDrag.y, e.getX(), e.getY());
                    newColoredShape.color = currentColor;
                    shapes.add(newColoredShape);
                    currentSelectedShape = shapes.size() - 1;
                } else if(currentClicked == 3) {
                    points.add(new Point(e.getX(), e.getY()));
                }

                startDrag = null;
                endDrag = null;
                repaint();
            }
        }

        /**
         *
         * This class contains mouseDragged method and controls mouse motion while dragged
         *
         */
        class DragMouse extends MouseMotionAdapter {
            /**
             *  This method sets actual mouse coordinates and controls moving and scaling
             */
            public void mouseDragged(MouseEvent e) {
                endDrag = new Point(e.getX(), e.getY());

                if(currentClicked == 4) {
                    for(int index = shapes.size(), temp = 0; index > 0 && temp == 0; index--) {
                        if(shapes.get(index - 1).isHit(endDrag.x, endDrag.y)) {
                            int deltaX = endDrag.x - middlePoint.x;
                            int deltaY = endDrag.y - middlePoint.y;
                            middlePoint.x = middlePoint.x + deltaX;
                            middlePoint.y = middlePoint.y + deltaY;
                            temp = 1;
                            shapes.get(index - 1).createTranslatedShape(shapes.get(index - 1).shape, deltaX, deltaY);
                            repaint();
                        }
                    }

                    middlePoint.x = endDrag.x;
                    middlePoint.y = endDrag.y;
                    changeLabel();
                } else if(currentClicked == 5 && scaleShapes[1].shape != null) {
                    if(scaleShapes[1].isHit(endDrag.x, endDrag.y)) {
                        int deltaX = endDrag.x - middlePoint.x;
                        int deltaY = endDrag.y - middlePoint.y;
                        middlePoint.x = middlePoint.x + deltaX;
                        middlePoint.y = middlePoint.y + deltaY;
                        scaleShapes[1].createTranslatedShape(scaleShapes[1].shape, deltaX, deltaY);
                        double differenceX = scaleShapes[1].shape.getBounds().getX() - startScaleX;
                        double differenceY = scaleShapes[1].shape.getBounds().getY() - startScaleY;
                        ColoredShape tempColoredShape = new ColoredShape(scaleShapes[0].shape, null);
                        shapes.get(currentSelectedShape).createScaledShape(tempColoredShape.shape, 1 + 0.006 * differenceX, 1 + 0.006 * differenceY);
                        changeLabel();
                    }
                }

              repaint();
            }
        }

        /**
         *  Method which paints all shapes
         */
        public void paint(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (ColoredShape s : shapes) {
                g2.setPaint(s.color);
                g2.fill(s.shape);
            }

            g2.setPaint(currentColor);

            if((points.size() > 1) && currentClicked == 3 && startDrag == null && endDrag == null) {
                Shape r = makePolygon(points, -1, -1);
                g2.fill(r);
            }

            if(currentClicked == 5 && currentSelectedShape >= 0 && scaleShapes[1].shape != null) {
                GradientPaint gp1 = new GradientPaint((int) scaleShapes[1].shape.getBounds().getX(), (int) scaleShapes[1].shape.getBounds().getY(), Color.RED, (int) scaleShapes[1].shape.getBounds().getX() + 30,(int) scaleShapes[1].shape.getBounds().getY() + 30, Color.GREEN);
                g2.setPaint(Color.BLACK);
                g2.fill(new Rectangle2D.Float((int) scaleShapes[1].shape.getBounds().getX() - 4, (int) scaleShapes[1].shape.getBounds().getY() - 4, 38, 38));
                g2.setPaint(gp1);
                g2.fill(scaleShapes[1].shape);
            }

            g2.setPaint(currentColor);

            if(startDrag != null && endDrag != null) {
                Shape r = null;
                if(currentClicked == 1) {
                    r = makeRectangle(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                    g2.fill(r);
                } else if(currentClicked == 2) {
                    r = makeEllipse(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                    g2.fill(r);
                } else if(currentClicked == 3 && points.size() > 1) {
                     r = makePolygon(points, endDrag.x, endDrag.y);
                     g2.fill(r);
                }
            }
        }


        /**
         *  Method that creates and return rectangle specified by parameters
         *
         * @param x1  x coordinate where mouse started movement
         * @param y1  y coordinate where mouse started movement
         * @param x2  x coordinate where mouse finished movement
         * @param y2  y coordinate where mouse finished movement
         * @return    returns rectangle specified by location and dimension
         */
        private Rectangle2D.Float makeRectangle(int x1, int y1, int x2, int y2) {
            return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
        }

        /**
         *  Method that creates and return ellipse specified by parameters
         *
         * @param x1  x coordinate where mouse started movement
         * @param y1  y coordinate where mouse started movement
         * @param x2  x coordinate where mouse finished movement
         * @param y2  y coordinate where mouse finished movement
         * @return    returns ellipse specified by framing rectangle location and size
         */
        private Ellipse2D.Float makeEllipse(int x1, int y1, int x2, int y2) {
            int radius = Math.min(Math.abs(x1 - x2), Math.abs(y1 - y2));
            return new Ellipse2D.Float(x1 - radius / 2,  y1 - radius / 2, radius, radius);
        }

        /**
         *  Method that creates and returns polygon
         *
         * @param pointsArray  ArrayList of points which describes polygon
         * @param x0      x coordinate where mouse finished movement
         * @param y0      y coordinate where mouse finished movement
         * @return       returns GeneralPath object specified by points
         */
        private GeneralPath.Float makePolygon(ArrayList<Point> pointsArray, int x0, int y0) {
            GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
            path.moveTo(pointsArray.get(0).x, pointsArray.get(0).y);

            for(int i = 1; i < pointsArray.size(); i++) {
                path.lineTo(pointsArray.get(i).x, pointsArray.get(i).y);
            }

            if(x0 > 0 && y0 > 0) {
                path.lineTo(x0, y0);
            }

            path.closePath();
            return path;
        }

        /**
         *   Method which updates the information about coordinates and size of shape
         */
        private void changeLabel() {
            String shapeHeight = Integer.toString((int) shapes.get(currentSelectedShape).shape.getBounds().getHeight());
            String shapeWidth = Integer.toString((int) shapes.get(currentSelectedShape).shape.getBounds().getWidth());
            String cX = Integer.toString((int) shapes.get(currentSelectedShape).shape.getBounds().getX());
            String cY = Integer.toString((int) shapes.get(currentSelectedShape).shape.getBounds().getY());
            labelSize.setText(stringDimensions + shapeHeight + " " + shapeWidth);
            labelCoordinates.setText(stringCoordinates + cX + " " + cY);
        }
    }


    /**
     *
     *  Class which contains information about shape and color
     *  and moving, scaling and hit-detecting methods
     *
     */
    class ColoredShape implements Serializable {
        private Shape shape;
        private Color color;

        public ColoredShape(Shape shape, Color color) {
            this.shape = shape;
            this.color = color;
        }

        /**
         *  Checks if shape is hit by mouse
         *
         * @param x   x coordinate where mouse was pressed
         * @param y   y coordinate where mouse was pressed
         * @return    true or false
         */
        public boolean isHit(float x, float y) {
            return shape.getBounds2D().contains(x, y);
        }

        /**
         *    Move shape by some x and y values
         *
         * @param shape    shape which is moved
         * @param transX   x shift value
         * @param transY   y shift value
         */
        public void createTranslatedShape(Shape shape, double transX, double transY) {
            final AffineTransform transform = AffineTransform.getTranslateInstance(transX, transY);
            this.shape = transform.createTransformedShape(shape);
        }

        /**
         * 	 Scale shape by factors
         *
         * @param shape    shape which is scaled
         * @param scaleX   x scale factor
         * @param scaleY   y scale factor
         */
        public void createScaledShape(Shape shape, double scaleX, double scaleY) {
            AffineTransform transform = AffineTransform.getScaleInstance(scaleX, scaleY);
            this.shape = transform.createTransformedShape(shape);
        }
    }


    /*
     *  Creates assistant rectangle for scaling
     */
    public Rectangle2D createScaleRectangle(Shape shape) {
        Rectangle2D rec = shape.getBounds2D();
        int x1 = (int) rec.getMaxX() + 30;
        int y1 = (int) rec.getMaxY() + 30;

        return new Rectangle2D.Float(x1, y1, 30, 30);
    }

    /**
     *  Creates ToolBar
     */
    private void createToolBar() {
        JPanel panelColors = new JPanel();
        FlowLayout newFlowLayout = new FlowLayout(FlowLayout.LEFT);
        panelColors.setLayout(newFlowLayout);
        panelColors.setPreferredSize(new Dimension(170, 58));

        Dimension colorButtonSize = new Dimension(22, 22);
        Dimension buttonSize = new Dimension(90, 42);
        ButtonGroup colorsButtonGroup = new ButtonGroup();

        JButton colorButtons[] = new JButton[12];
        Color Colors[] = {Color.BLACK, Color.GRAY, Color.LIGHT_GRAY, Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, new Color(153,102,0), Color.YELLOW, Color.CYAN};

        JButton buttonCurrentColor = new JButton();
        buttonCurrentColor.setPreferredSize(new Dimension(30, 30));
        buttonCurrentColor.setBackground(Color.BLACK);

        /**
         *  Changes selected color
         */
        ActionListener colorButtonsActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
                currentColor = abstractButton.getBackground();
                buttonCurrentColor.setBackground(currentColor);

                if(currentSelectedShape >= 0 && (currentClicked == 4 || currentClicked == 5)) {
                    shapes.get(currentSelectedShape).color = currentColor;
                }

                repaint();
            }
        };


        for(int i = 0; i < colorButtons.length; i++) {
            colorButtons[i] = new JButton();
            colorButtons[i].setBackground(Colors[i]);
            colorButtons[i].setPreferredSize(colorButtonSize);
            colorsButtonGroup.add(colorButtons[i]);
            panelColors.add(colorButtons[i]);
            colorButtons[i].addActionListener(colorButtonsActionListener);
        }

        colorButtons[0].setSelected(true);
        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));

        ButtonGroup buttonsGroup = new ButtonGroup();
        JToggleButton buttonRectangle = new JToggleButton("Rectangle");
        buttonRectangle.setPreferredSize(buttonSize);
        JToggleButton buttonCircle = new JToggleButton("Circle");
        buttonCircle.setPreferredSize(buttonSize);
        JToggleButton buttonPolygon = new JToggleButton("Polygon");
        buttonPolygon.setPreferredSize(buttonSize);
        JToggleButton buttonMove = new JToggleButton("Move");
        buttonMove.setPreferredSize(buttonSize);
        JToggleButton buttonScale = new JToggleButton("Scale");
        buttonScale.setPreferredSize(buttonSize);

        JLabel selectedColor = new JLabel("Selected color: ");
        JButton infoButton = new JButton("Info");


        toolbar.addSeparator();
        toolbar.add(buttonRectangle);
        buttonRectangle.setSelected(true);
        toolbar.addSeparator();
        toolbar.add(buttonCircle);
        toolbar.addSeparator();
        toolbar.add(buttonPolygon);
        toolbar.addSeparator();
        toolbar.add(buttonMove);
        toolbar.addSeparator();
        toolbar.add(buttonScale);
        toolbar.addSeparator();

        toolbar.add(panelColors);
        toolbar.addSeparator();

        toolbar.add(selectedColor);
        toolbar.add(buttonCurrentColor);
        toolbar.addSeparator(new Dimension(46, 54));
        toolbar.add(infoButton);
        toolbar.addSeparator();

        toolbar.setFloatable(false);

        buttonsGroup.add(buttonRectangle);
        buttonsGroup.add(buttonCircle);
        buttonsGroup.add(buttonPolygon);
        buttonsGroup.add(buttonMove);
        buttonsGroup.add(buttonScale);
        add(toolbar, BorderLayout.NORTH);

        /**
         *
         *   ActionListeners for shape, moving and scaling buttons
         *
         */
        ActionListener actionListenerRectangle = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                currentClicked = 1;
                labelSize.setText(" ");
                labelCoordinates.setText(" ");
                repaint();
            }
        };

        ActionListener actionListenerCircle = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                currentClicked = 2;
                labelSize.setText(" ");
                labelCoordinates.setText(" ");
                repaint();
            }
        };

        ActionListener actionListenerPolygon = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                points.clear();
                currentClicked = 3;
                labelSize.setText(" ");
                labelCoordinates.setText(" ");
                repaint();
            }
        };

        ActionListener actionListenerMove = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                currentClicked = 4;
                repaint();
            }
        };

        ActionListener actionListenerScale = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                currentClicked = 5;
            }
        };


        buttonRectangle.addActionListener(actionListenerRectangle);
        buttonCircle.addActionListener(actionListenerCircle);
        buttonPolygon.addActionListener(actionListenerPolygon);
        buttonMove.addActionListener(actionListenerMove);
        buttonScale.addActionListener(actionListenerScale);



        /*
         * Info dialog
         *
         */
        ActionListener actionListenerInfo = new ActionListener() {
            public void actionPerformed(ActionEvent ae){
                JPanel infoPanel = new JPanel();
                JLabel labelName = new JLabel("Name: Simple Graphics Editor");
                JLabel labelAuthor = new JLabel("Author: Jakub Bachanek");
                JLabel labelDescription = new JLabel("Description: Creating and editing geometric figures");
                infoPanel.setLayout(new GridLayout(0, 1, 0, 12));
                infoPanel.add(labelName);
                infoPanel.add(labelAuthor);
                infoPanel.add(labelDescription);
                Font f = new Font("TimesRoman",Font.BOLD,14);
                labelName.setFont(f);
                labelAuthor.setFont(f);
                labelDescription.setFont(f);
                JOptionPane.showMessageDialog(null, infoPanel, "Info", JOptionPane.PLAIN_MESSAGE);
            }
        };

        infoButton.addActionListener(actionListenerInfo);
    }


    /*
     *  Class containing menu where you can save and load shapes
     *
     */
    class MyMenu implements Serializable {
        JMenu menu;
        JMenuBar menuBar = new JMenuBar();
        JMenuItem saveItem, loadItem;

        MyMenu() {
            menu=new JMenu("Menu");
            saveItem = new JMenuItem("Save");
            loadItem = new JMenuItem("Load");

            saveItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    save();
                }
            });

            loadItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    load();
                }
            });

            menu.add(saveItem);
            menu.add(loadItem);
            menuBar.add(menu);
        }
    }



    EditorFrame() {
        super("Simple Graphics Editor");
        setBounds(360,40,1020,810);
        this.setJMenuBar(myMenu.menuBar);
        panelMain.setBackground(Color.WHITE);
        add(panelMain);
        add(panelBottom, BorderLayout.SOUTH);
        panelBottom.setLayout(new FlowLayout(FlowLayout.LEFT));
        panelBottom.add(labelSize);
        panelBottom.add(labelCoordinates);
        createToolBar();
        addWindowListener(new MyWindowAdapter());
        setResizable(true);
    }

    /**
     *  Method for saving shapes into file
     */
    public void save() {
        try {
            FileOutputStream saveFile = new FileOutputStream("SaveFile");
            ObjectOutputStream save = new ObjectOutputStream(saveFile);
            save.writeObject(shapes);
            save.close();
            saveFile.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /*
     *  Method for loading shapes from file
     */
    public void load() {
        try {
            FileInputStream readFile = new FileInputStream("SaveFile");
            ObjectInputStream read = new ObjectInputStream(readFile);
            shapes = (ArrayList<ColoredShape>) read.readObject();
            repaint();
            read.close();
            readFile.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

}




/*--------------------------------------------------------------------*/

public class Program {
    public static void main(String[] args) {
        EditorFrame editorFrame = new EditorFrame();
        editorFrame.setVisible(true);
    }
}
