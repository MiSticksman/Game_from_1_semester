package ru.vsu.cs.course1.Game;


import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ru.vsu.cs.util.JTableUtils;
import ru.vsu.cs.util.SwingUtils;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;

public class MainForm extends JFrame {
    private JPanel panelMain;
    private JTable tableGameField;
    private JPanel panelTop;
    private JLabel labelCountOfMoves;

    private JButton buttonChooseLevel;
    private JButton buttonRestart;
    private JLabel labelLevel;
    private JButton buttonNextLevel;
    private JPanel panelTime;
    private JLabel labelTime;


    private static final int DEFAULT_GAP = 10;
    private static final int DEFAULT_CELL_SIZE = 50;


    private GameLevels params = new GameLevels(1);
    private GameEngine game = new GameEngine();
    private LevelChooserDialog levelChooserDialog;
    private int time = 0;
    private Timer timer = new Timer(1000, e -> {
        if (game.getState() == GameEngine.GameState.PLAYING) {
            time++;
            this.labelTime.setText("" + time);
        }
    });


    public MainForm() {
        this.setTitle("Выключить свет!");
        this.setContentPane(panelMain);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();

        setJMenuBar(createMenuBar());
        this.pack();

        SwingUtils.setShowMessageDefaultErrorHandler();


        tableGameField.setRowHeight(DEFAULT_CELL_SIZE);
        JTableUtils.initJTableForArray(tableGameField, DEFAULT_CELL_SIZE, false, false, false, false);
        tableGameField.setIntercellSpacing(new Dimension(0, 0));
        tableGameField.setEnabled(true);

        levelChooserDialog = new LevelChooserDialog(params, this);


        tableGameField.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            final class DrawComponent extends Component {
                private int row = 0, column = 0;

                @Override
                public void paint(Graphics gr) {
                    Graphics2D g2d = (Graphics2D) gr;
                    int width = getWidth() - 2;
                    int height = getHeight() - 2;
                    paintCell(row, column, g2d, width, height);
                }
            }

            DrawComponent comp = new DrawComponent();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                comp.row = row;
                comp.column = column;
                return comp;
            }
        });

        newGame();

        updateWindowSize();

        this.setBounds(400, 200, 325, 435);

        updateView();


        buttonChooseLevel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                levelChooserDialog.updateView();
                levelChooserDialog.setVisible(true);
            }

        });
        buttonRestart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newGame();
            }
        });
        buttonNextLevel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                params.setLevelNumber(Math.min(params.getLevelNumber() + 1, params.getCountOfLevels()));
                newGame();
            }
        });
        tableGameField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int row = tableGameField.rowAtPoint(e.getPoint());
                int col = tableGameField.columnAtPoint(e.getPoint());
                if (SwingUtilities.isLeftMouseButton(e)) {
                    game.leftMouseClick(row, col);
                    updateView();
                }
            }
        });
    }

    public void startGame() {
        newGame();
        updateView();

    }

    private JMenuItem createMenuItem(String text, String shortcut, Character mnemonic, ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(listener);
        if (shortcut != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(shortcut.replace('+', ' ')));
        }
        if (mnemonic != null) {
            menuItem.setMnemonic(mnemonic);
        }
        return menuItem;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBarMain = new JMenuBar();

        JMenu menuGame = new JMenu("Игра");
        menuBarMain.add(menuGame);
        menuGame.add(createMenuItem("Новая", "ctrl+N", null, e -> {
            newGame();
        }));
        menuGame.addSeparator();
        menuGame.add(createMenuItem("Выход", "ctrl+X", null, e -> {
            System.exit(0);
        }));

        JMenu menuView = new JMenu("Вид");
        menuBarMain.add(menuView);
        menuView.add(createMenuItem("Подогнать размер окна", null, null, e -> {
            updateWindowSize();
        }));
        menuView.addSeparator();
        SwingUtils.initLookAndFeelMenu(menuView);

        JMenu menuHelp = new JMenu("Справка");
        menuBarMain.add(menuHelp);
        menuHelp.add(createMenuItem("Правила", "ctrl+R", null, e -> {
            SwingUtils.showInfoMessageBox("Один из вариантов игры «Выключить свет!»" +
                            "\nДля победы Вам необходимо погасить все лампочки. Удачи!",
                    "Правила");
        }));
        menuHelp.add(createMenuItem("О программе", "ctrl+A", null, e -> {
            SwingUtils.showInfoMessageBox(
                    "Игра «Выключить свет!»" +
                            "\n– пример логической игры для 1-го курса ФКН ВГУ" +
                            "\n\nАвтор: Шафоростов В.Ю." +
                            "\nE-mail: vadimschaforostov@ya.ru",
                    "О программе"
            );
        }));

        return menuBarMain;
    }

    private void updateWindowSize() {
        int menuSize = this.getJMenuBar() != null ? this.getJMenuBar().getHeight() : 0;
        SwingUtils.setFixedSize(
                this,
                tableGameField.getWidth() + 2 * DEFAULT_GAP + 60,
                tableGameField.getHeight() + panelMain.getY() + panelTop.getHeight() + labelCountOfMoves.getHeight() +
                        menuSize + 3 * DEFAULT_GAP + 2 * DEFAULT_GAP + 60
        );
        this.setMaximumSize(null);
        this.setMinimumSize(null);
    }

    private void updateView() {
        labelCountOfMoves.setText("Количество шагов: " + game.getCountOfMoves());
        labelLevel.setText("Выбранный уровень: " + params.getLevelNumber());
        tableGameField.repaint();
        if (game.getState() == GameEngine.GameState.WIN) {
            SwingUtils.showInfoMessageBox(
                    "Использовано шагов: " + game.getCountOfMoves(),
                    "Победа!"
            );
            newGame();
        }

    }

    private void paintCell(int row, int column, Graphics2D g2d, int cellWidth, int cellHeight) {
        boolean cell = game.check(row, column);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(cellWidth, cellHeight);

        Color backColor = cell ? new Color(123, 255, 241, 255) : new Color(129, 172, 188, 54);
        g2d.setColor(backColor);
        //g2d.fillRect(0, 0, size, size);
        g2d.fillOval(0, 0, size, size);

    }

    private void newGame() {
        game.newGame(params);
        JTableUtils.resizeJTable(tableGameField,
                game.getRowCount(), game.getColCount(),
                tableGameField.getRowHeight(), tableGameField.getRowHeight()
        );
        this.setBounds(getX(), getY(), Math.max(tableGameField.getWidth() + 40, 325), tableGameField.getHeight() + 185);
        tableGameField.grabFocus();
        updateView();
        time = 0;
        timer.start();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelMain = new JPanel();
        panelMain.setLayout(new GridLayoutManager(4, 3, new Insets(10, 10, 10, 10), -1, 10));
        panelTop = new JPanel();
        panelTop.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.add(panelTop, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonChooseLevel = new JButton();
        buttonChooseLevel.setText("Выбрать уровень");
        panelTop.add(buttonChooseLevel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonRestart = new JButton();
        buttonRestart.setText("Рестарт");
        panelTop.add(buttonRestart, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelTime = new JPanel();
        panelTime.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panelTop.add(panelTime, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        labelTime = new JLabel();
        labelTime.setText("0");
        panelTime.add(labelTime, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panelTime.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panelTime.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        Font scrollPane1Font = this.$$$getFont$$$("Bodoni MT Black", -1, 12, scrollPane1.getFont());
        if (scrollPane1Font != null) scrollPane1.setFont(scrollPane1Font);
        panelMain.add(scrollPane1, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(500, 500), null, 0, false));
        tableGameField = new JTable();
        scrollPane1.setViewportView(tableGameField);
        labelCountOfMoves = new JLabel();
        labelCountOfMoves.setText("Количество шагов");
        panelMain.add(labelCountOfMoves, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelLevel = new JLabel();
        labelLevel.setText("Выберите уровень.");
        panelMain.add(labelLevel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonNextLevel = new JButton();
        buttonNextLevel.setText("Следующий уровень");
        panelMain.add(buttonNextLevel, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panelMain.add(spacer3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panelMain.add(spacer4, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }

}
