/**
 *  Simulation, random colors change
 *  @author Jakub Bachanek
 *  @version 1.0
 */


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


class MyWindowAdapter extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }
}



class SimulationFrame extends JFrame {
    Grid newGrid;
    JButton buttons[][];
    double msNumMin;
    double msNumMax;
    JPanel gridPanel = new JPanel();
    
    SimulationFrame(int width, int height, double probability, int duration) {
        super("Colors simulation");
        setBounds(36, 40, 10 , 10);
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        msNumMin = 0.6 * duration;
        msNumMax = 1.2 * duration;
        buttons = new JButton[height][width];
        gridPanel.setLayout(new GridLayout(height, width, 0, 0));

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setPreferredSize(new Dimension(12, 12));
                gridPanel.add(buttons[i][j]);
            }
        }
         
        add(gridPanel);
        newGrid = new Grid(height, width, probability, this);
        pack();
        addWindowListener(new MyWindowAdapter());
        setResizable(false);
    }
}



class SettingsFrame extends JFrame {    
    JPanel panel_1 = new JPanel();
    JPanel panel_2 = new JPanel();
    JPanel panel_3 = new JPanel();

    void buttonPressed(int width, int height, double probability, int duration) {
        SimulationFrame simulationFrame = new SimulationFrame(width, height, probability, duration);
        this.setVisible(false);
        simulationFrame.setVisible(true);
    }
    
    SettingsFrame() {
        super("Settings");
        setBounds(640,300,330,250);

        Font f = new Font("", Font.BOLD, 18);
        JLabel labelWidth = new JLabel("Width: ");
        JLabel labelHeight = new JLabel("Height: ");
        JLabel labelProbability = new JLabel("Probability: ");
        JLabel labelCycleDuration = new JLabel("Cycle duration: ");
        labelWidth.setFont(f);
        labelHeight.setFont(f);
        labelProbability.setFont(f);
        labelCycleDuration.setFont(f);

        JTextField textfieldWidth = new JTextField("60", 4);
        JTextField textfieldHeight = new JTextField("40", 4);
        JTextField textfieldProbability = new JTextField("0.2", 4);
        JTextField textfieldCycleDuration = new JTextField("200", 4);
          
        textfieldWidth.setPreferredSize(new Dimension(30, 30));
        textfieldHeight.setPreferredSize(new Dimension(30, 30));
        textfieldProbability.setPreferredSize(new Dimension(30, 30));
        textfieldCycleDuration.setPreferredSize(new Dimension(30, 30));
        textfieldWidth.setFont(f);
        textfieldHeight.setFont(f);
        textfieldProbability.setFont(f);
        textfieldCycleDuration.setFont(f);
          
        JButton buttonStart = new JButton("START");
        buttonStart.setFont(new Font("", Font.BOLD, 16));
          
        buttonStart.setPreferredSize(new Dimension(120, 40));
        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                buttonPressed( Integer.parseInt(textfieldWidth.getText()), Integer.parseInt(textfieldHeight.getText()), Double.parseDouble(textfieldProbability.getText()), Integer.parseInt(textfieldCycleDuration.getText()));
          }
        });
          
        panel_1.setLayout(new GridLayout(0, 1, 0, 10));
        panel_2.setLayout(new GridLayout(0, 1, 0, 4));
        panel_1.setBackground(new Color(0, 188, 250));
        panel_2.setBackground(new Color(0, 188, 250));
        panel_3.setBackground(new Color(0, 188, 250));
        getContentPane().setBackground(new Color(0, 188, 250));
          
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
          
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(panel_1, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(panel_2, gbc);
        panel_1.add(labelWidth);
        panel_2.add(textfieldWidth);
        panel_1.add(labelHeight);
        panel_2.add(textfieldHeight);
        panel_1.add(labelProbability);
        panel_2.add(textfieldProbability);
        panel_1.add(labelCycleDuration);
        panel_2.add(textfieldCycleDuration);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(panel_3, gbc);
        panel_3.add(buttonStart);
          
        addWindowListener(new MyWindowAdapter());
        setResizable(false);
    }
}


public class Program {
    public static void main(String[] args) {
        SettingsFrame settings = new SettingsFrame();
        settings.setVisible(true);
    }
}
