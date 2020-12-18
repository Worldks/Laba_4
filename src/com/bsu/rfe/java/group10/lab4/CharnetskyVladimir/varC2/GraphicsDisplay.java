package com.bsu.rfe.java.group10.lab4.CharnetskyVladimir.varC2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;
import java.awt.geom.*;//for AffineTransform

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel{
    private Double[][] graphicsData;
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = false;
    private boolean showCoordinateGrid = false;
    private boolean showLeft90DegreeRotation = false;
    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Используемый масштаб отображения
    private double scale;
    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private BasicStroke coordinateGridStrokeAlongX;
    private BasicStroke coordinateGridStrokeAlongY;
    // Различные шрифты отображения надписей
    private Font axisFont;
    private Font gridFont;

    private int numberOfDecimalPlaces = 0;

    public GraphicsDisplay() {
        setBackground(Color.WHITE); // Цвет заднего фона области отображения - белый
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, new float [] {6,6}, 0.0f);     // Перо для рисования графика
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);     // Перо для рисования осей координат
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);     // Перо для рисования контуров маркеров
        axisFont = new Font("Serif", Font.BOLD, 36);                         // Шрифт для подписей осей координат
        gridFont = new Font("Serif",Font.BOLD,20);
    }
    // Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
    // главного окна приложения в случае успешной загрузки данных
    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;       // Сохранить массив точек во внутреннем поле класса
        repaint();      // Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
    }
    // Методы-модификаторы для изменения параметров отображения графика
    // Изменение любого параметра приводит к перерисовке области
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }
    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    public void setShowCoordinateGrid(boolean showCoordinateGrid){
        this.showCoordinateGrid = showCoordinateGrid;
        repaint();
    }

    public void setShowLeft90DegreeRotation(boolean showLeft90DegreeRotation){
        this.showLeft90DegreeRotation = showLeft90DegreeRotation;
        repaint();
    }

    /* Метод-помощник, осуществляющий преобразование координат.
  * Оно необходимо, т.к. верхнему левому углу холста с координатами
  * (0.0, 0.0) соответствует точка графика с координатами (minX, maxY),
  где
  * minX - это самое "левое" значение X, а
  * maxY - самое "верхнее" значение Y.
  */
    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;       // Вычисляем смещение X от самой левой точки (minX)
        double deltaY = maxY - y;       // Вычисляем смещение Y от точки верхней точки (maxY)
        return new Point2D.Double(deltaX*scale, deltaY*scale);
    }
    /* Метод-помощник, возвращающий экземпляр класса Point2D.Double
     * смещённый по отношению к исходному на deltaX, deltaY
     * К сожалению, стандартного метода, выполняющего такую задачу, нет.
     */
    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        Point2D.Double dest = new Point2D.Double();     // Инициализировать новый экземпляр точки
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);     // Задать её координаты как координаты существующей точки + заданные смещения
        return dest;
    }

    // Отрисовка графика по прочитанным координатам
    protected void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(graphicsStroke);       // Выбрать линию для рисования графика
        canvas.setColor(Color.RED);         // Выбрать цвет линии
        GeneralPath graphics = new GeneralPath();
        for (int i=0; i<graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            if (i>0) {
                graphics.lineTo(point.getX(), point.getY());
            } else {
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graphics);
    }

    // Метод, обеспечивающий отображение осей координат
    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(axisStroke);   // Установить особое начертание для осей
        canvas.setColor(Color.BLACK);   // Оси рисуются чѐрным цветом
        canvas.setPaint(Color.BLACK);   // Стрелки заливаются чѐрным цветом
        canvas.setFont(axisFont);   // Подписи к координатным осям делаются специальным шрифтом
        FontRenderContext context = canvas.getFontRenderContext();// Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
        // Определить, должна ли быть видна ось Y на графике
        if (minX<=0.0 && maxX>=0.0) {
// Она должна быть видна, если левая граница показываемой области (minX) <= 0.0,
            // а правая (maxX) >= 0.0
// Сама ось - это линия между точками (0, maxY) и (0, minY)
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
            GeneralPath arrow = new GeneralPath();      // Стрелка оси Y
            Point2D.Double lineEnd = xyToPoint(0, maxY);// Установить начальную точку ломаной точно на верхний конец оси Y
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            // Вести левый "скат" стрелки в точку с относительными координатами (5,20)
            arrow.lineTo(arrow.getCurrentPoint().getX()+5, arrow.getCurrentPoint().getY()+20);
            // Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
            arrow.lineTo(arrow.getCurrentPoint().getX()-10, arrow.getCurrentPoint().getY());
            arrow.closePath();  // Замкнуть треугольник стрелки
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси Y
// Определить, сколько места понадобится для надписи "y"
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("y", (float)labelPos.getX() + 10,
                    (float)(labelPos.getY() - bounds.getY()));
        }
        //------------------------------------------------------------------------------
        Rectangle2D bs = axisFont.getStringBounds(stringNumberWithoutTrash(0.0),context);
        Point2D.Double labelPos_1 = xyToPoint(0,0);
        // Вывести надписи в точке с вычисленными координатами
        canvas.drawString(stringNumberWithoutTrash(0.0),(float)labelPos_1.getX()+10,
                (float)(labelPos_1.getY()-bs.getY()));
        //------------------------------------------------------------------------------
        // Определить, должна ли быть видна ось X на графике
        if (minY<=0.0 && maxY>=0.0) {
// Она должна быть видна, если верхняя граница показываемой области (maxX) >= 0.0,
// а нижняя (minY) <= 0.0
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));
            GeneralPath arrow = new GeneralPath();  // Стрелка оси X
// Установить начальную точку ломаной точно на правый конец оси X
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести верхний "скат" стрелки в точку с относительными координатами (-20,-5)
            arrow.lineTo(arrow.getCurrentPoint().getX()-20,
                    arrow.getCurrentPoint().getY()-5);
// Вести левую часть стрелки в точку с относительными координатами (0, 10)
            arrow.lineTo(arrow.getCurrentPoint().getX(),
                    arrow.getCurrentPoint().getY()+10);
            arrow.closePath();  // Замкнуть треугольник стрелки
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси X
// Определить, сколько места понадобится для надписи "x"
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("x", (float)(labelPos.getX() -
                    bounds.getWidth() - 10), (float)(labelPos.getY() + bounds.getY()));
        }
    }

    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);
        for (Double[] point: graphicsData) {

            if (point[1].intValue()%2 == 0 && point[1].intValue() != 0)
                canvas.setColor(Color.red);
            else
                canvas.setColor(Color.blue);

            Point2D.Double center = xyToPoint(point[0], point[1]);
            GeneralPath path = new GeneralPath();
            path.moveTo(center.getX() - 5.5,center.getY());
            path.lineTo(center.getX() + 5.5,center.getY());
            path.moveTo(center.getX(),center.getY() - 5.5);
            path.lineTo(center.getX(),center.getY() + 5.5);
            path.moveTo(center.getX() - 2.75,center.getY() - 5.5);
            path.lineTo(center.getX() + 2.75,center.getY() - 5.5);
            path.moveTo(center.getX() - 2.75,center.getY() + 5.5);
            path.lineTo(center.getX() + 2.75,center.getY() + 5.5);
            path.moveTo(center.getX() - 5.5,center.getY() - 2.75);
            path.lineTo(center.getX() - 5.5,center.getY() + 2.75);
            path.moveTo(center.getX() + 5.5,center.getY() - 2.75);
            path.lineTo(center.getX() + 5.5,center.getY() + 2.75);
            canvas.draw(path); // Начертить контур маркера
        }
    }

    public void turnLeft90(Graphics2D canvas){
        // матрица перехода плюс смещенные координатны начальной точки
        double w = getWidth();
        double h = getHeight();
        AffineTransform at = new AffineTransform(0,-1*(h/w),1*(w/h),0,0,getHeight());
        canvas.transform(at);
    }

    // Реализация отображения сетки
    public void paintCoordinateGrid(Graphics2D canvas){
        double lengthY = maxY - minY; // Определение цены деления по оси Y
        final double divisionValueY = findDivisionValue(lengthY);
        double lengthX = maxX - minX; // Определение цены деления по оси X
        final double divisionValueX = findDivisionValue(lengthX);
        // Нахождение длины в пикселях для шаблона линии
        double lengthLineAlongXInPixel = divisionValueX*scale;
        double lengthLineAlongYInPixel = divisionValueY*scale;
        // Нахождение цены одного деления линни в шаблоне в пикселях
        float x1 = (float)lengthLineAlongXInPixel/20;
        float y1 = (float)lengthLineAlongYInPixel/20;
        // Задания линии исходя из наших параметров
        coordinateGridStrokeAlongX = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,10.0f,
                new float[]{x1,x1,x1,x1,x1,x1,x1,x1,3*x1,x1,x1,x1,x1,x1,x1,x1,x1,x1},0.0f);
        coordinateGridStrokeAlongY = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,10.0f,
                new float[]{y1,y1,y1,y1,y1,y1,y1,y1,3*y1,y1,y1,y1,y1,y1,y1,y1,y1,y1},0.0f);
        // Установить особое начертания для линией вдоль X
        canvas.setColor(Color.GRAY);
        canvas.setFont(gridFont);
        // Создать объект контекста отображения текста - для получения
        // характеристик устройства (экрана)
        FontRenderContext context = canvas.getFontRenderContext();
        // Нарисуем линии координатной сетки вдоль X
        if(showCoordinateGrid){         //maxX>0 && minX<0&&maxY>0&&minY<0
            //Нарисуем координатные линии вдоль X
            canvas.setStroke(coordinateGridStrokeAlongX);
            Double currentCoordinateY = divisionValueY;
            while(currentCoordinateY < maxY){
                canvas.draw(new Line2D.Double(xyToPoint(0,currentCoordinateY),xyToPoint(minX,currentCoordinateY)));
                canvas.draw(new Line2D.Double(xyToPoint(0,currentCoordinateY),xyToPoint(maxX,currentCoordinateY)));
                Rectangle2D bounds = gridFont.getStringBounds(stringNumberWithoutTrash(currentCoordinateY),context);
                Point2D.Double labelPos = xyToPoint(0,currentCoordinateY);
                // Вывести надписи в точке с вычисленными координатами
                canvas.drawString(stringNumberWithoutTrash(currentCoordinateY),(float)labelPos.getX()+10,
                        (float)(labelPos.getY()-bounds.getY()));
                currentCoordinateY += divisionValueY;
            }
            currentCoordinateY = -divisionValueY;
            while(currentCoordinateY > minY){
                canvas.draw(new Line2D.Double(xyToPoint(0,currentCoordinateY),xyToPoint(minX,currentCoordinateY)));
                canvas.draw(new Line2D.Double(xyToPoint(0,currentCoordinateY),xyToPoint(maxX,currentCoordinateY)));
                Rectangle2D bounds = gridFont.getStringBounds(stringNumberWithoutTrash(currentCoordinateY),context);
                Point2D.Double labelPos = xyToPoint(0,currentCoordinateY);
                canvas.drawString(stringNumberWithoutTrash(currentCoordinateY),(float)labelPos.getX()+10,
                        (float)(labelPos.getY()+bounds.getY()));
                currentCoordinateY -= divisionValueY;
            }
            //Нарисуем координатнуые линни вдоль Y
            canvas.setStroke(coordinateGridStrokeAlongY);
            Double currentCoordinateX = divisionValueX;
            while(currentCoordinateX < maxX){
                canvas.draw(new Line2D.Double(xyToPoint(currentCoordinateX,0),xyToPoint(currentCoordinateX,maxY)));
                canvas.draw(new Line2D.Double(xyToPoint(currentCoordinateX,0),xyToPoint(currentCoordinateX,minY)));
                Rectangle2D bounds = gridFont.getStringBounds(stringNumberWithoutTrash(currentCoordinateX),context);
                Point2D.Double labelPos = xyToPoint(currentCoordinateX,0);
                canvas.drawString(stringNumberWithoutTrash(currentCoordinateX),
                        (float)(labelPos.getX()-bounds.getX()-25),(float)labelPos.getY()-10);
                currentCoordinateX += divisionValueX;

            }
            currentCoordinateX = -divisionValueX;
            while(currentCoordinateX > minX){
                canvas.draw(new Line2D.Double(xyToPoint(currentCoordinateX,0),xyToPoint(currentCoordinateX,maxY)));
                canvas.draw(new Line2D.Double(xyToPoint(currentCoordinateX,0),xyToPoint(currentCoordinateX,minY)));
                Rectangle2D bounds = gridFont.getStringBounds(stringNumberWithoutTrash(currentCoordinateX),context);
                Point2D.Double labelPos = xyToPoint(currentCoordinateX,0);
                canvas.drawString(stringNumberWithoutTrash(currentCoordinateX),(float)(labelPos.getX()+bounds.getX()),
                        (float)labelPos.getY()-10);
                currentCoordinateX -= divisionValueX;
            }
        }
    }

    // Метод помощник для строкового представления числа без мусора
    private String stringNumberWithoutTrash(Double number){
        String num = number.toString();
        if(num.length() > 10) {
            String returnNum = "";
            int i = 0;
            while (num.charAt(i) != '.') {
                returnNum += num.charAt(i);
                i++;
            }
            returnNum += num.charAt(i);
            i++;
            for (int k = i; k <= i + numberOfDecimalPlaces; k++) {
                returnNum += num.charAt(k);
            }
            return returnNum;
        }else return num;
    }
    // Метод-помощник для  нахождения ены деления
    private double findDivisionValue(double length){
        Double del = length/14;
        if(del>=1){
            Integer delInt = del.intValue();
            String delS = delInt.toString();
            return (delS.charAt(0)-'0')*Math.pow(10,delS.length()-1);
        }else{
            String delS = del.toString();
            int i=2;
            while(delS.charAt(i) == '0'){
                i++;
            }
            if(numberOfDecimalPlaces<i-1) {
                numberOfDecimalPlaces = i - 1;
            }
            return (delS.charAt(i)-'0')*Math.pow(10,-(i-1));
        }
    }

    // Метод отображения всего компонента, содержащего график
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphicsData==null || graphicsData.length==0) return;// Шаг 2 - Если данные графика не загружены (при показе компонента при запуске программы) - ничего не делать
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length-1][0];
        minY = graphicsData[0][1];
        maxY = minY;
        for (int i = 1; i < graphicsData.length; i++) {// Найти минимальное и максимальное значение функции
            if (graphicsData[i][1]<minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1]>maxY) {
                maxY = graphicsData[i][1];
            }
        }
/* Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X
и Y - сколько пикселов
* приходится на единицу длины по X и по Y
*/
        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);
// Шаг 5 - Чтобы изображение было неискажѐнным - масштаб должен быть одинаков
// Выбираем за основу минимальный
        scale = Math.min(scaleX, scaleY);
// Шаг 6 - корректировка границ отображаемой области согласно выбранному масштабу
        if (scale==scaleX) {
/* Если за основу был взят масштаб по оси X, значит по оси Y
делений меньше,
* т.е. подлежащий визуализации диапазон по Y будет меньше
высоты окна.
* Значит необходимо добавить делений, сделаем это так:
* 1) Вычислим, сколько делений влезет по Y при выбранном
масштабе - getSize().getHeight()/scale
* 2) Вычтем из этого сколько делений требовалось изначально
* 3) Набросим по половине недостающего расстояния на maxY и
minY
*/
            double yIncrement = (getSize().getHeight()/scale - (maxY - minY))/2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale==scaleY) {
// Если за основу был взят масштаб по оси Y, действовать по аналогии
            double xIncrement = (getSize().getWidth()/scale - (maxX - minX))/2;
            maxX += xIncrement;
            minX -= xIncrement;
        }
// Шаг 7 - Сохранить текущие настройки холста
        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
// Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
// Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет затираться последующим
// Первыми (если нужно) отрисовываются оси координат.
        if (showLeft90DegreeRotation) turnLeft90(canvas);
        if (showAxis) paintAxis(canvas);
// Затем отображается сам график
        paintGraphics(canvas);
// Затем (если нужно) отображаются маркеры точек, по которым строился график.
        if (showMarkers) paintMarkers(canvas);
        if (showCoordinateGrid) paintCoordinateGrid(canvas);
// Шаг 9 - Восстановить старые настройки холста
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }
}