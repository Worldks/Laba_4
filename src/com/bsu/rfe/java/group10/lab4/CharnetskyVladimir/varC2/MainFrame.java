package com.bsu.rfe.java.group10.lab4.CharnetskyVladimir.varC2;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

@SuppressWarnings("serial")
public class MainFrame extends JFrame{


    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private JFileChooser fileChooser = null;
    private JCheckBoxMenuItem showAxisMenuItem;
    private JCheckBoxMenuItem showMarkersMenuItem;
    private GraphicsDisplay display = new GraphicsDisplay();
    private boolean fileLoaded = false;

    public MainFrame() {
        super("Построение графиков функций на основе заранее подготовленных файлов");// Вызов конструктора предка Frame
        setSize(WIDTH, HEIGHT);// Установка размеров окна
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - WIDTH)/2, (kit.getScreenSize().height - HEIGHT)/2);// Отцентрировать окно приложения на экране
        setExtendedState(MAXIMIZED_BOTH); // ---------Развѐртывание окна на весь экран--------
        JMenuBar menuBar = new JMenuBar();// Создать и установить полосу меню
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("Файл");// Добавить пункт меню "Файл"
        menuBar.add(fileMenu);
        Action openGraphicsAction = new AbstractAction("Открыть файл с  графиком") {   // Создать действие по открытию файла
            public void actionPerformed(ActionEvent event) {
                if (fileChooser==null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    openGraphics(fileChooser.getSelectedFile());
            }
        };
        fileMenu.add(openGraphicsAction);    // Добавить соответствующий элемент меню
        JMenu graphicsMenu = new JMenu("График");        // Создать пункт меню "График"
        menuBar.add(graphicsMenu);
        Action showAxisAction = new AbstractAction("Показывать оси координат") {// Создать действие для реакции на активацию элемента "Показывать оси координат"
            public void actionPerformed(ActionEvent event) {
            display.setShowAxis(showAxisMenuItem.isSelected());
            }
        };
        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
        graphicsMenu.add(showAxisMenuItem);    // Добавить соответствующий элемент в меню
        showAxisMenuItem.setSelected(true);    // Элемент по умолчанию включен (отмечен флажком)
        Action showMarkersAction = new AbstractAction("Показывать маркеры точек") {// Повторить действия для элемента "Показывать маркеры точек"
            public void actionPerformed(ActionEvent event) {
                display.setShowMarkers(showMarkersMenuItem.isSelected());// по аналогии с showAxisMenuItem
            }
        };
        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
        graphicsMenu.add(showMarkersMenuItem);
        showMarkersMenuItem.setSelected(true);// Элемент по умолчанию включен (отмечен флажком)
        graphicsMenu.addMenuListener(new GraphicsMenuListener());// Зарегистрировать обработчик событий, связанных с меню "График"
        getContentPane().add(display, BorderLayout.CENTER);// Установить GraphicsDisplay в цент граничной компоновки
    }
// Считывание данных графика из существующего файла
    protected void openGraphics(File selectedFile) {
        try {
// Шаг 1 - Открыть поток чтения данных, связанный с входным файловым потоком
        DataInputStream in = new DataInputStream(new
        FileInputStream(selectedFile));
/* Шаг 2 - Зная объѐм данных в потоке ввода можно вычислить,
* сколько памяти нужно зарезервировать в массиве:
* Всего байт в потоке - in.available() байт;
* Размер одного числа Double - Double.SIZE бит, или
Double.SIZE/8 байт;
* Так как числа записываются парами, то число пар меньше в
2 раза
*/
        Double[][] graphicsData = new
        Double[in.available()/(Double.SIZE/8)/2][];
// Шаг 3 - Цикл чтения данных (пока в потоке есть данные)
        int i = 0;
        while (in.available()>0) {
// Первой из потока читается координата точки X
        Double x = in.readDouble();
// Затем - значение графика Y в точке X
        Double y = in.readDouble();
// Прочитанная пара координат добавляется в массив
        graphicsData[i++] = new Double[] {x, y};
        }
// Шаг 4 - Проверка, имеется ли в списке в результате чтения хотя бы одна пара координат
        if (graphicsData!=null && graphicsData.length>0) {
// Да - установить флаг загруженности данных
        fileLoaded = true;
// Вызывать метод отображения графика
        display.showGraphics(graphicsData);
        }
// Шаг 5 - Закрыть входной поток
        in.close();
        } catch (FileNotFoundException ex) {
// В случае исключительной ситуации типа "Файл не найден" показать сообщение об ошибке
        JOptionPane.showMessageDialog(MainFrame.this, "Указанный файл не найден", "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
        return;
        } catch (IOException ex) {
// В случае ошибки ввода из файлового потока показать сообщение об ошибке
        JOptionPane.showMessageDialog(MainFrame.this, "Ошибка чтения координат точек из файла", "Ошибка загрузки данных",
        JOptionPane.WARNING_MESSAGE);
        return;
        }
        }
    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
// Класс-слушатель событий, связанных с отображением меню
    private class GraphicsMenuListener implements MenuListener {
    // Обработчик, вызываемый перед показом меню
        public void menuSelected(MenuEvent e) {
// Доступность или недоступность элементов меню "График" определяется загруженностью данных
            showAxisMenuItem.setEnabled(fileLoaded);
            showMarkersMenuItem.setEnabled(fileLoaded);
        }
    // Обработчик, вызываемый после того, как меню исчезло с экрана
    public void menuDeselected(MenuEvent e) {
        }
// Обработчик, вызываемый в случае отмены выбора пункта меню (очень редкая ситуация)
    public void menuCanceled(MenuEvent e) {
        }
    }
}
