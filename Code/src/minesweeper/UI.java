package minesweeper;


import javax.swing.*;
import java.awt.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;


public class UI extends JFrame
{
    // The buttons
    private JButton[][] botones;
    
    // Number of Buttons in Grid
    private int filas;
    private int columnas;
    
    // Labels 
    private JLabel etiquemina;
    private int minas;
    
    private JLabel tiempopasadonivel;    
    private Thread temporizador;
    private int tiempopasado;
    private boolean pararTemporizador;
    
    // Frame settings
    private final String FRAME_TITLE = "Minesweeper ~ Developed by Haris Muneer";
    
    private int Ancho_Cuadro = 520;
    private int altura_Cuadro = 550;
    private int FRAME_LOC_X = 430;
    private int FRAME_LOC_Y = 50;

    // Icons
    private Icon minaroja;
    private Icon mina;
    private Icon bandera;
    private Icon titulo;
    
    
    // Menu Bar and Items
    
    private JMenuBar menuBar;
    private JMenu menuJuego;
    private JMenuItem nuevojuego;
    private JMenuItem estadística;
    private JMenuItem salir;

    
    
    //---------------------------------------------------------------//
    public UI(int r, int c, int m)
    {                
        this.filas = r;
        this.columnas = c;
        
        botones = new JButton [filas][columnas];

        // Set frame
        setSize(Ancho_Cuadro, altura_Cuadro);
        setTitle(FRAME_TITLE);
        setLocation(FRAME_LOC_X, FRAME_LOC_Y);
               
        // The layout of the frame:

        JPanel gameBoard;        
        JPanel tmPanel;        
        JPanel scorePanel;
        
        //----------------GAME BOARD---------------------//
        // Build the "gameBoard".
        gameBoard = new JPanel();
        gameBoard.setLayout(new GridLayout(filas,columnas,0,0));
        
        for( int y=0 ; y<filas ; y++ ) 
        {
            for( int x=0 ; x<columnas ; x++ ) 
            {
                // Set button text.
                botones[x][y] = new JButton("");

                // Set button name (x,y).
                botones[x][y].setName(Integer.toString(x) + "," + Integer.toString(y));
                botones[x][y].setFont(new Font("Serif", Font.BOLD, 24));
                
                botones[x][y].setBorder(BorderFactory.createLineBorder(Color.black, 1, true));

                // Add this button to the gameboard.
                gameBoard.add(botones[x][y]);
            }
        }
        //-----------------------------------------------//
                
                
        //-------------TIME AND MINE------------------------//
        
        JPanel timePassedPanel = new JPanel();
        timePassedPanel.setLayout(new BorderLayout(10,0));
        
        // Initialize the time passed label.
        this.tiempopasadonivel = new JLabel ("  0  " , SwingConstants.CENTER);
        tiempopasadonivel.setFont(new Font("Serif", Font.BOLD, 20));
                
        Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        
        tiempopasadonivel.setBorder(loweredetched);
        tiempopasadonivel.setBackground(new Color(110,110,255));
        tiempopasadonivel.setForeground(Color.white);
        tiempopasadonivel.setOpaque(true);
        
        JLabel iT = new JLabel("",SwingConstants.CENTER);
        iT.setIcon(new ImageIcon(getClass().getResource("/resources/clock.png"))); 

        timePassedPanel.add(iT, BorderLayout.WEST);
        timePassedPanel.add(tiempopasadonivel, BorderLayout.CENTER);
        timePassedPanel.setOpaque(false);
        
        this.tiempopasado = 0;
        this.pararTemporizador = true;

        
        JPanel minesPanel = new JPanel();
        minesPanel.setLayout(new BorderLayout(10,0));
        
        
        // Initialize mines label.
        this.etiquemina = new JLabel ("  0  " , SwingConstants.CENTER);
        etiquemina.setFont(new Font("Serif", Font.BOLD, 20));
        etiquemina.setBorder(loweredetched);
        etiquemina.setBackground(new Color(110,110,255));
        etiquemina.setForeground(Color.white);
        
        etiquemina.setOpaque(true);
        setMines(m);
        
        JLabel mT = new JLabel("", SwingConstants.CENTER);
        mT.setIcon(new ImageIcon(getClass().getResource("/resources/mine.png")));

        minesPanel.add(etiquemina, BorderLayout.WEST);
        minesPanel.add(mT, BorderLayout.CENTER);
        minesPanel.setOpaque(false);
        
        // Build the "tmPanel".
        tmPanel = new JPanel();
        tmPanel.setLayout(new BorderLayout(0,20));
        
        tmPanel.add(timePassedPanel, BorderLayout.WEST);
        tmPanel.add(minesPanel, BorderLayout.EAST);
        tmPanel.setOpaque(false);
        
        //--------------------------------------------//
                        
        
        //------------------Menu--------------------------//
        menuBar = new JMenuBar();
        
        menuJuego = new JMenu("Juego");
         
        nuevojuego = new JMenuItem("   New Game");
        estadística = new JMenuItem("   Statistics");
        salir = new JMenuItem("   Exit");

        nuevojuego.setName("New Game");
        estadística.setName("Statistics");
        salir.setName("Exit");

        menuJuego.add(nuevojuego);
        menuJuego.add(estadística);
        menuJuego.add(salir);
        
        menuBar.add(menuJuego);                        
        //----------------------------------------------------//
               
        
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(0,10));
        p.add(gameBoard, BorderLayout.CENTER);
        p.add(tmPanel, BorderLayout.SOUTH);
    
 
        p.setBorder(BorderFactory.createEmptyBorder(60, 60, 14, 60));        
        p.setOpaque(false);
      
        
        setLayout(new BorderLayout());
        JLabel background = new JLabel(new ImageIcon(getClass().getResource("/resources/2.jpg")));
        
        add(background);        
        
        background.setLayout(new BorderLayout(0,0));
        
        background.add(menuBar,BorderLayout.NORTH);
        background.add(p, BorderLayout.CENTER);        
        
        
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/mine.png")));
               
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
	
    //-----------------------------------------------------------------//

    //-----------------------Related to Timer------------------------//
    
    // Starts the timer
    public void startTimer()
    {        
        pararTemporizador = false;
        
        temporizador = new Thread() {
                @Override
                public void run()
                {
                    while(!pararTemporizador)
                    {
                        tiempopasado++;

                        // Update the time passed label.
                        tiempopasadonivel.setText("  " + tiempopasado + "  ");

                        // Wait 1 second.
                        try{
                            sleep(1000); 
                        }
                        catch(InterruptedException ex){}
                    }
                }
        };                

       temporizador.start();
    }

    
    public void interruptTimer()
    {
        pararTemporizador = true;
                
        try 
        {
            if (temporizador!= null)
                temporizador.join();
        } 
        catch (InterruptedException ex) 
        {

        }        
    }
    
    public void resetTimer()
    {
        tiempopasado = 0;
        tiempopasadonivel.setText("  " + tiempopasado + "  ");        
    }

    public void setTimePassed(int t)
    {
        tiempopasado = t;
        tiempopasadonivel.setText("  " + tiempopasado + "  ");                
    }
    
    //-----------------------------------------------------------//
    
    
    public void initGame()
    {
        hideAll();
        enableAll();
    }
    
    //------------------HELPER FUNCTIONS-----------------------//

    //Makes buttons clickable
    public void enableAll()
    {
        for( int x=0 ; x<columnas ; x++ ) 
        {
            for( int y=0 ; y<filas ; y++ ) 
            {
                botones[x][y].setEnabled(true);
            }
        }
    }

    //Makes buttons non-clickable
    public void disableAll()
    {
        for( int x=0 ; x<columnas ; x++ ) 
        {
            for( int y=0 ; y<filas ; y++ ) 
            {
                botones[x][y].setEnabled(false);
            }
        }
    }


    //Resets the content of all buttons
    public void hideAll()
    {
        for( int x=0 ; x<columnas ; x++ ) 
        {
            for( int y=0 ; y<filas ; y++ ) 
            {
                botones[x][y].setText("");                
                botones[x][y].setBackground(new Color(0,103,200));
                botones[x][y].setIcon(titulo);                
            }
        }
    }

    
    //---------------SET LISTENERS--------------------------//
    
    public void setButtonListeners(Juego game)
    {
        addWindowListener(game);
    
        // Set listeners for all buttons in the grid in gameBoard
        for( int x=0 ; x<columnas ; x++ ) 
        {
            for( int y=0 ; y<filas ; y++ ) 
            {
                botones[x][y].addMouseListener(game);
            }
        }
        
        // Set listeners for menu items in menu bar
       nuevojuego.addActionListener(game);
       estadística.addActionListener(game);
       salir.addActionListener(game);

       nuevojuego.setAccelerator(KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
       salir.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
       estadística.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));       
    }
    
    
    //-----------------GETTERS AND SETTERS--------------------//
    
    public JButton[][] getButtons()
    {
        return botones;
    }
    
    public int getTimePassed()
    {
        return tiempopasado;
    }    


    //----------------------SET LOOK------------------------------//
    
    public static void setLook(String look)
    {
        try {

            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (look.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            
        } catch (Exception ex) { }            
    }

    //-------------------------------------------------------------//
    
    public void setMines(int m)
    {
        minas = m;
        etiquemina.setText("  " + Integer.toString(m) + "  ");
    }
    
    public void incMines()
    {
        minas++;
        setMines(minas);
    }
    
    public void decMines()
    {
        minas--;
        setMines(minas);
    }
    
    public int getMines()
    {
        return minas;
    }
            
    //--------------------Related to Icons----------------------------//
    private static Icon resizeIcon(ImageIcon icon, int resizedWidth, int resizedHeight) 
    {
        Image img = icon.getImage();  
        Image resizedImage = img.getScaledInstance(resizedWidth, resizedHeight,  java.awt.Image.SCALE_SMOOTH);  
        return new ImageIcon(resizedImage);
    }    
    
    public void setIcons()
    {
       //---------------------Set Icons-----------------------------//

        int bOffset = botones[0][1].getInsets().left;
        int bWidth = botones[0][1].getWidth();
        int bHeight = botones[0][1].getHeight();
        
        ImageIcon d;
        
        d = new ImageIcon(getClass().getResource("/resources/redmine.png"));                
        minaroja =   resizeIcon(d, bWidth - bOffset, bHeight - bOffset);        

        d = new ImageIcon(getClass().getResource("/resources/mine.png"));                
        mina =   resizeIcon(d, bWidth - bOffset, bHeight - bOffset);        
        
        d = new ImageIcon(getClass().getResource("/resources/flag.png"));                
        bandera =   resizeIcon(d, bWidth - bOffset, bHeight - bOffset);        
        
        d = new ImageIcon(getClass().getResource("/resources/tile.png"));                
        titulo =   resizeIcon(d, bWidth - bOffset, bHeight - bOffset);        
                
        //-------------------------------------------------------//
        
    }
    
    public Icon getIconMine()
    {
        return mina;
    }

    public Icon getIconRedMine()
    {
        return minaroja;
    }
    
    public Icon getIconFlag()
    {
        return bandera;
    }
    
    public Icon getIconTile()
    {
        return titulo;       
    }        
    
    
    //---------------------------------------------------------------------//
    public void setTextColor(JButton b)
    {
        if (b.getText().equals("1"))
            b.setForeground(Color.blue);
        else if (b.getText().equals("2"))
            b.setForeground(new Color(76,153,0));
        else if (b.getText().equals("3"))
            b.setForeground(Color.red);
        else if (b.getText().equals("4"))
            b.setForeground(new Color(153,0,0));
        else if (b.getText().equals("5"))
            b.setForeground(new Color(153,0,153));
        else if (b.getText().equals("6"))
            b.setForeground(new Color(96,96,96));
        else if (b.getText().equals("7"))
            b.setForeground(new Color(0,0,102));
        else if (b.getText().equals("8"))
            b.setForeground(new Color(153,0,76));        
    }
    //------------------------------------------------------------------------//
    
    
}
