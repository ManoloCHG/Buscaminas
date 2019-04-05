package minesweeper;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.sql.Date;
import javafx.util.Pair;
import javax.swing.border.TitledBorder;
import minesweeper.Puntuacion.Time;



// This is the main controller class
public class Juego implements MouseListener, ActionListener, WindowListener
{
    public static String dbPath;
    // "playing" indicates whether a game is running (true) or not (false).
    private boolean jugando; 

    private Tabla tabla;

    private UI pepe;
    
    private Puntuacion puntuacion;
        
    //------------------------------------------------------------------//        

    /**
     *@autor Manolo
     * @Version final Español
     */

    public Juego()
    {
        // set db path
        String p = "";

        try 
        {
            p = new File(Juego.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath() + "\\db.accdb";
        }
        catch (URISyntaxException ex) 
        {
            System.out.println("Error al cargar el archivo de base de datos.");
        }

        dbPath =   "jdbc:ucanaccess://" + p;

        
        puntuacion = new Puntuacion();
        puntuacion.populate();
        
        UI.setLook("Nimbus");
                        
        creartabla();
        
        this.pepe = new UI(tabla.getRows(), tabla.getCols(), tabla.getNumberOfMines());        
        this.pepe.setButtonListeners(this);
                        
        this.jugando = false;
        
        pepe.setVisible(true);
        
        pepe.setIcons();        
        pepe.hideAll();
        
        resumenJuego();
    }

    //-----------------Load Save Game (if any)--------------------------//
    
    public void resumenJuego()
    {
        if(tabla.checkSave())
        {
            ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));      

            int option = JOptionPane.showOptionDialog(null, "¿Quieres continuar con el juego anterior?", 
                            "Juego guardado encontrado", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, question,null,null);

            switch(option) 
            {
                case JOptionPane.YES_OPTION:      
      
                    //load board's state
                    Pair p = tabla.loadSaveGame();
                    
                    //set button's images
                    establecerimagenesbotones();
                    
                    //load timer's value                                        
                    pepe.setTimePassed((int)p.getKey());

                    //load mines value
                    pepe.setMines((int)p.getValue());
                    
                    pepe.startTimer();
                    
                    jugando = true;
                    break;

                case JOptionPane.NO_OPTION:
                    tabla.deleteSavedGame();
                    break;
                    
                case JOptionPane.CLOSED_OPTION:
                    tabla.deleteSavedGame();
                    break;
            }
        }
    }


    //-------------------------------------------------//
    public void establecerimagenesbotones()
    {
        celda cells[][] = tabla.getCells();
        JButton buttons[][] = pepe.getButtons();
        
        for( int y=0 ; y<tabla.getRows() ; y++ ) 
        {
            for( int x=0 ; x<tabla.getCols() ; x++ ) 
            {
                buttons[x][y].setIcon(null);
                
                if (cells[x][y].getContent().equals(""))
                {
                    buttons[x][y].setIcon(pepe.getIconTile());
                }
                else if (cells[x][y].getContent().equals("F"))
                {
                    buttons[x][y].setIcon(pepe.getIconFlag());
                    buttons[x][y].setBackground(Color.blue);	                    
                }
                else if (cells[x][y].getContent().equals("0"))
                {
                    buttons[x][y].setBackground(Color.lightGray);
                }
                else
                {
                    buttons[x][y].setBackground(Color.lightGray);                    
                    buttons[x][y].setText(cells[x][y].getContent());
                    pepe.setTextColor(buttons[x][y]);                                        
                }
            }
        }
    }
    
    
    //------------------------------------------------------------//
        
    public void creartabla()
    {
        // Create a new board        
        int mines = 10;

        int r = 9;
        int c = 9;
                
        this.tabla = new Tabla(mines, r, c);        
    }
    

    //---------------------------------------------------------------//
    public void nuevoJuego()
    {                
        this.jugando = false;        
                                
        creartabla();
        
        pepe.interruptTimer();
        pepe.resetTimer();        
        pepe.initGame();
        pepe.setMines(tabla.getNumberOfMines());
    }
    //------------------------------------------------------------------------------//
    
    public void reiniciarJuego()
    {
        this.jugando = false;
        
        tabla.resetBoard();
        
        pepe.interruptTimer();
        pepe.resetTimer();        
        pepe.initGame();
        pepe.setMines(tabla.getNumberOfMines());
    }
        
    //------------------------------------------------------------------------------//    
    private void finJuego()
    {
        jugando = false;
        MostrarTodo();

        puntuacion.save();
    }

    
    //-------------------------GAME WON AND GAME LOST ---------------------------------//
    
    public void JuegoGanado()
    {
        puntuacion.incCurrentStreak();
        puntuacion.incCurrentWinningStreak();
        puntuacion.incGamesWon();
        puntuacion.incGamesPlayed();
        
        pepe.interruptTimer();
        finJuego();
        //----------------------------------------------------------------//
        
        
        JDialog dialog = new JDialog(pepe, Dialog.ModalityType.DOCUMENT_MODAL);
        
        //------MESSAGE-----------//
        JLabel message = new JLabel("Congratulaciones, Has ganado", SwingConstants.CENTER);
                
        //-----STATISTICS-----------//
        JPanel statistics = new JPanel();
        statistics.setLayout(new GridLayout(6,1,0,10));
        
        ArrayList<Time> bTimes = puntuacion.getBestTimes();
        
        if (bTimes.isEmpty() || (bTimes.get(0).getTimeValue() > pepe.getTimePassed()))
        {
            statistics.add(new JLabel("    obtubiste el mejor tiempo para este nivel de dificultad    "));
        }
        
        puntuacion.addTime(pepe.getTimePassed(), new Date(System.currentTimeMillis()));
                
        JLabel time = new JLabel("  Tiempo:  " + Integer.toString(pepe.getTimePassed()) + " segundos            Fecha:  " + new Date(System.currentTimeMillis()));
        
        JLabel bestTime = new JLabel();
        
        
        if (bTimes.isEmpty())
        {
            bestTime.setText("  Mejor Tiempo:  ---                  Fecha:  ---");
        }
        else
        {
            bestTime.setText("  Mejor Tiempo:  " + bTimes.get(0).getTimeValue() + " segundos            Fecha:  " + bTimes.get(0).getDateValue());
        }
        
        JLabel gPlayed = new JLabel("  Partidas Jugadas:  " + puntuacion.getGamesPlayed());
        JLabel gWon = new JLabel("  Partidas Ganadas:  " + puntuacion.getGamesWon());
        JLabel gPercentage = new JLabel("  Win Percentage:  " + puntuacion.getWinPercentage() + "%");
        
        statistics.add(time);
        statistics.add(bestTime);
        statistics.add(gPlayed);
        statistics.add(gWon);
        statistics.add(gPercentage);
        
        Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);        
        statistics.setBorder(loweredetched);
        
        
        //--------BUTTONS----------//
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1,2,10,0));
        
        JButton exit = new JButton("Salir");
        JButton playAgain = new JButton("Jugar de nuevo");

        
        exit.addActionListener((ActionEvent e) -> {
            dialog.dispose();
            windowClosing(null);
        });        
        playAgain.addActionListener((ActionEvent e) -> {
            dialog.dispose();            
            nuevoJuego();
        });        
        
        
        buttons.add(exit);
        buttons.add(playAgain);
        
        //--------DIALOG-------------//
        
        JPanel c = new JPanel();
        c.setLayout(new BorderLayout(20,20));
        c.add(message, BorderLayout.NORTH);
        c.add(statistics, BorderLayout.CENTER);
        c.add(buttons, BorderLayout.SOUTH);
        
        c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    dialog.dispose();
                    nuevoJuego();
            }
            }
        );

        dialog.setTitle("Partida Ganada");
        dialog.add(c);
        dialog.pack();
        dialog.setLocationRelativeTo(pepe);
        dialog.setVisible(true);                        
    }
    
    public void JugoPerdido()
    {
        puntuacion.decCurrentStreak();
        puntuacion.incCurrentLosingStreak();
        puntuacion.incGamesPlayed();
        
        pepe.interruptTimer();
        
        finJuego();
        
        //----------------------------------------------------------------//

        JDialog dialog = new JDialog(pepe, Dialog.ModalityType.DOCUMENT_MODAL);
        
        //------MESSAGE-----------//
        JLabel message = new JLabel("Lo sientos, perdiste el juego. Mejor prueva con el tetris!", SwingConstants.CENTER);
                
        //-----STATISTICS-----------//
        JPanel statistics = new JPanel();
        statistics.setLayout(new GridLayout(5,1,0,10));
        
        JLabel time = new JLabel("  Tiempo:  " + Integer.toString(pepe.getTimePassed()) + " segundos");
        
        JLabel bestTime = new JLabel();
        
        ArrayList<Time> bTimes = puntuacion.getBestTimes();
        
        if (bTimes.isEmpty())
        {
            bestTime.setText("                        ");
        }
        else
        {
            bestTime.setText("  Mejor Tiempo:  " + bTimes.get(0).getTimeValue() + " segundos            Fecha:  " + bTimes.get(0).getDateValue());
        }
        
        JLabel gPlayed = new JLabel("   Partidas Jugadas:  " + puntuacion.getGamesPlayed());
        JLabel gWon = new JLabel("  Partidads ganadas:  " + puntuacion.getGamesWon());
        JLabel gPercentage = new JLabel("  Porcentaje de victoria:  " + puntuacion.getWinPercentage() + "%");
        
        statistics.add(time);
        statistics.add(bestTime);
        statistics.add(gPlayed);
        statistics.add(gWon);
        statistics.add(gPercentage);
        
        Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);        
        statistics.setBorder(loweredetched);
        
        
        //--------BUTTONS----------//
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1,3,2,0));
        
        JButton exit = new JButton("Salida");
        JButton restart = new JButton("Reiniciar");
        JButton playAgain = new JButton("Jugar de Nuevo");

        
        exit.addActionListener((ActionEvent e) -> {
            dialog.dispose();
            windowClosing(null);
        });        
        restart.addActionListener((ActionEvent e) -> {
            dialog.dispose();            
            reiniciarJuego();
        });        
        playAgain.addActionListener((ActionEvent e) -> {
            dialog.dispose();            
            nuevoJuego();
        });        
        
        
        buttons.add(exit);
        buttons.add(restart);
        buttons.add(playAgain);
        
        //--------DIALOG-------------//
        
        JPanel c = new JPanel();
        c.setLayout(new BorderLayout(20,20));
        c.add(message, BorderLayout.NORTH);
        c.add(statistics, BorderLayout.CENTER);
        c.add(buttons, BorderLayout.SOUTH);
        
        c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    dialog.dispose();
                    nuevoJuego();
            }
            }
        );
        
        dialog.setTitle("Derrota");
        dialog.add(c);
        dialog.pack();
        dialog.setLocationRelativeTo(pepe);
        dialog.setVisible(true);        
    }
    
    
    //--------------------------------SCORE BOARD--------------------------------------//
    public void MostrarPuntuacion()
    {
        //----------------------------------------------------------------//
                
        JDialog dialog = new JDialog(pepe, Dialog.ModalityType.DOCUMENT_MODAL);

        //-----BEST TIMES--------//
        
        JPanel bestTimes = new JPanel();
        bestTimes.setLayout(new GridLayout(5,1));
        
        ArrayList<Time> bTimes = puntuacion.getBestTimes();
        
        for (int i = 0; i < bTimes.size(); i++)
        {
            JLabel t = new JLabel("  " + bTimes.get(i).getTimeValue() + "           " + bTimes.get(i).getDateValue());            
            bestTimes.add(t);
        }
        
        if (bTimes.isEmpty())
        {
            JLabel t = new JLabel("                               ");            
            bestTimes.add(t);
        }
        
        TitledBorder b = BorderFactory.createTitledBorder("Mejores Tiempos");
        b.setTitleJustification(TitledBorder.LEFT);

        bestTimes.setBorder(b);
                
        //-----STATISTICS-----------//
        JPanel statistics = new JPanel();
        
        statistics.setLayout(new GridLayout(6,1,0,10));        
        
        JLabel gPlayed = new JLabel("  Partidas Jugadas:  " + puntuacion.getGamesPlayed());
        JLabel gWon = new JLabel("  Juegos Ganados:  " + puntuacion.getGamesWon());
        JLabel gPercentage = new JLabel("  Porcentaje de victoria:  " + puntuacion.getWinPercentage() + "%");
        JLabel lWin = new JLabel("Mayor racha de victorias:  " + puntuacion.getLongestWinningStreak());
        JLabel lLose = new JLabel("Mayor Racha de derrotas:  " + puntuacion.getLongestLosingStreak());
        JLabel currentStreak = new JLabel("Racha actual:  " + puntuacion.getCurrentStreak());

        
        statistics.add(gPlayed);
        statistics.add(gWon);
        statistics.add(gPercentage);
        statistics.add(lWin);
        statistics.add(lLose);
        statistics.add(currentStreak);
                        
        Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);        
        statistics.setBorder(loweredetched);
        
        
        //--------BUTTONS----------//
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1,2,10,0));
        
        JButton close = new JButton("Cerrar");
        JButton reset = new JButton("Reiniciar");

        
        close.addActionListener((ActionEvent e) -> {
            dialog.dispose();
        });        
        reset.addActionListener((ActionEvent e) -> {
            ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));      

            int option = JOptionPane.showOptionDialog(null, "¿Quieres restablecer todas las estadisticas a cero?", 
                            "Reset Statistics", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, question,null,null);

            switch(option) 
            {
                case JOptionPane.YES_OPTION:      

                    puntuacion.resetScore();
                    puntuacion.save();
                    dialog.dispose();
                    MostrarPuntuacion();
                    break;

                case JOptionPane.NO_OPTION: 
                    break;
            }
        });        
        
        buttons.add(close);
        buttons.add(reset);
        
        if (puntuacion.getGamesPlayed() == 0)
            reset.setEnabled(false);
        
        //--------DIALOG-------------//
        
        JPanel c = new JPanel();
        c.setLayout(new BorderLayout(20,20));
        c.add(bestTimes, BorderLayout.WEST);
        c.add(statistics, BorderLayout.CENTER);        
        c.add(buttons, BorderLayout.SOUTH);
        
        c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        dialog.setTitle("Minesweeper Statistics - Haris Muneer");
        dialog.add(c);
        dialog.pack();
        dialog.setLocationRelativeTo(pepe);
        dialog.setVisible(true);                        
    }
    
    //------------------------------------------------------------------------------//
	
        
    // Shows the "solution" of the game.
    private void MostrarTodo()
    {
        String cellSolution;
        
        celda cells[][] = tabla.getCells();
        JButton buttons[][] = pepe.getButtons();

        for (int x=0; x<tabla.getCols(); x++ ) 
        {
            for (int y=0; y<tabla.getRows(); y++ ) 
            {
                cellSolution = cells[x][y].getContent();

                // Is the cell still unrevealed
                if( cellSolution.equals("") ) 
                {
                    buttons[x][y].setIcon(null);
                    
                    // Get Neighbours
                    cellSolution = Integer.toString(cells[x][y].getSurroundingMines());

                    // Is it a mine?
                    if(cells[x][y].getMine()) 
                    {
                        cellSolution = "M";
                        
                        //mine
                        buttons[x][y].setIcon(pepe.getIconMine());
                        buttons[x][y].setBackground(Color.lightGray);                        
                    }
                    else
                    {
                        if(cellSolution.equals("0"))
                        {
                            buttons[x][y].setText("");                           
                            buttons[x][y].setBackground(Color.lightGray);
                        }
                        else
                        {
                            buttons[x][y].setBackground(Color.lightGray);
                            buttons[x][y].setText(cellSolution);
                            pepe.setTextColor(buttons[x][y]);
                        }
                    }
                }

                // This cell is already flagged!
                else if( cellSolution.equals("F") ) 
                {
                    // Is it correctly flagged?
                    if(!cells[x][y].getMine()) 
                    {
                        buttons[x][y].setBackground(Color.orange);
                    }
                    else
                        buttons[x][y].setBackground(Color.green);
                }
                
            }
        }
    }
    

    //-------------------------------------------------------------------------//
    
    //-------------------------------------------------------------------------//    
    

    //-------------------------------------------------------------------------//

    
    //--------------------------------------------------------------------------//
    
    public boolean isFinished()
    {
        boolean isFinished = true;
        String cellSolution;

        celda cells[][] = tabla.getCells();
        
        for( int x = 0 ; x < tabla.getCols() ; x++ ) 
        {
            for( int y = 0 ; y < tabla.getRows() ; y++ ) 
            {
                // If a game is solved, the content of each Cell should match the value of its surrounding mines
                cellSolution = Integer.toString(cells[x][y].getSurroundingMines());
                
                if(cells[x][y].getMine()) 
                    cellSolution = "F";

                // Compare the player's "answer" to the solution.
                if(!cells[x][y].getContent().equals(cellSolution))
                {
                    //This cell is not solved yet
                    isFinished = false;
                    break;
                }
            }
        }

        return isFinished;
    }

 
    //Check the game to see if its finished or not
    private void checkGame()
    {		
        if(isFinished()) 
        {            
            JuegoGanado();
        }
    }
   
    //----------------------------------------------------------------------/
       
    
    /*
     * If a player clicks on a zero, all surrounding cells ("neighbours") must revealed.
     * This method is recursive: if a neighbour is also a zero, his neighbours must also be revealed.
     */
    public void findZeroes(int xCo, int yCo)
    {
        int neighbours;
        
        celda cells[][] = tabla.getCells();
        JButton buttons[][] = pepe.getButtons();

        // Columns
        for(int x = tabla.makeValidCoordinateX(xCo - 1) ; x <= tabla.makeValidCoordinateX(xCo + 1) ; x++) 
        {			
            // Rows
            for(int y = tabla.makeValidCoordinateY(yCo - 1) ; y <= tabla.makeValidCoordinateY(yCo + 1) ; y++) 
            {
                // Only unrevealed cells need to be revealed.
                if(cells[x][y].getContent().equals("")) 
                {
                    // Get the neighbours of the current (neighbouring) cell.
                    neighbours = cells[x][y].getSurroundingMines();

                    // Reveal the neighbours of the current (neighbouring) cell
                    cells[x][y].setContent(Integer.toString(neighbours));

                    if (!cells[x][y].getMine())
                        buttons[x][y].setIcon(null);                        
                    
                    // Is this (neighbouring) cell a "zero" cell itself?
                    if(neighbours == 0)
                    {                        
                        // Yes, give it a special color and recurse!
                        buttons[x][y].setBackground(Color.lightGray);
                        buttons[x][y].setText("");
                        findZeroes(x, y);
                    }
                    else
                    {
                        // No, give it a boring gray color.
                        buttons[x][y].setBackground(Color.lightGray);
                        buttons[x][y].setText(Integer.toString(neighbours));
                        pepe.setTextColor(buttons[x][y]);                        
                    }
                }
            }
        }
    }
    //-----------------------------------------------------------------------------//
    //This function is called when clicked on closed button or exit
    @Override
    public void windowClosing(WindowEvent e) 
    {
        if (jugando)
        {
            ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));      

            Object[] options = {"Save","Don't Save","Cancel"};

            int quit = JOptionPane.showOptionDialog(null, "What do you want to do with the game in progress?", 
                            "New Game", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, question, options, options[2]);

            switch(quit) 
            {
                //save
                case JOptionPane.YES_OPTION:
                    
                    pepe.interruptTimer();
                    puntuacion.save();
                    
                    JDialog dialog = new JDialog(pepe, Dialog.ModalityType.DOCUMENT_MODAL);
                    JPanel panel = new JPanel();
                    panel.setLayout(new BorderLayout());
                    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                    panel.add(new JLabel("Saving.... Please Wait", SwingConstants.CENTER));
                    dialog.add(panel);
                    dialog.setTitle("Saving Game...");
                    dialog.pack();
                    dialog.setLocationRelativeTo(pepe);                    
                    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    
                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){
                       @Override
                       protected Void doInBackground() throws Exception 
                       {
                            tabla.saveGame(pepe.getTimePassed(), pepe.getMines());                
                            return null;
                       }
                       
                       @Override
                       protected void done(){
                           dialog.dispose();                           
                       }                       
                    };
                            
                    worker.execute();
                    dialog.setVisible(true);
                                                            
                    System.exit(0);
                    break;
                
                //dont save                    
                case JOptionPane.NO_OPTION:
                    puntuacion.incGamesPlayed();
                    puntuacion.save();
                    System.exit(0);
                    break;
                    
                case JOptionPane.CANCEL_OPTION: break;
            }
        }
        else
            System.exit(0);
    }
    
    //-----------------------------------------------------------------------//

    @Override
    public void actionPerformed(ActionEvent e) {        
        JMenuItem menuItem = (JMenuItem) e.getSource();

        if (menuItem.getName().equals("New Game"))
        {
            if (jugando)
            {
                ImageIcon question = new ImageIcon(getClass().getResource("/resources/question.png"));      

                Object[] options = {"Quit and Start a New Game","Restart","Keep Playing"};
                
                int startNew = JOptionPane.showOptionDialog(null, "What do you want to do with the game in progress?", 
                                "New Game", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, question, options, options[2]);

                switch(startNew) 
                {
                    case JOptionPane.YES_OPTION:      
                        
                        // Initialize the new game.
                        nuevoJuego();
                        puntuacion.incGamesPlayed();
                        puntuacion.save();
                        break;

                    case JOptionPane.NO_OPTION: 
                        puntuacion.incGamesPlayed();   
                        puntuacion.save();
                        reiniciarJuego();
                        break;
                    
                    case JOptionPane.CANCEL_OPTION: break;
                }
            }
        }
        
        else if (menuItem.getName().equals("Exit"))
        {
            windowClosing(null);
        }
        
        //Statistics
        else
        {
            MostrarPuntuacion();
        }        
    }
    
    
    //--------------------------------------------------------------------------//
        
    //Mouse Click Listener
    @Override
    public void mouseClicked(MouseEvent e)
    {
        // start timer on first click
        if(!jugando)
        {
            pepe.startTimer();
            jugando = true;
        }
        
        if (jugando)
        {
            //Get the button's name
            JButton button = (JButton)e.getSource();

            // Get coordinates (button.getName().equals("x,y")).
            String[] co = button.getName().split(",");

            int x = Integer.parseInt(co[0]);
            int y = Integer.parseInt(co[1]);

            // Get cell information.
            boolean isMine = tabla.getCells()[x][y].getMine();
            int neighbours = tabla.getCells()[x][y].getSurroundingMines();

            // Left Click
            if (SwingUtilities.isLeftMouseButton(e)) 
            {
                if (!tabla.getCells()[x][y].getContent().equals("F"))
                {
                    button.setIcon(null);

                    //Mine is clicked.
                    if(isMine) 
                    {  
                        //red mine
                        button.setIcon(pepe.getIconRedMine());
                        button.setBackground(Color.red);
                        tabla.getCells()[x][y].setContent("M");

                        JugoPerdido();
                    }
                    else 
                    {
                        // The player has clicked on a number.
                        tabla.getCells()[x][y].setContent(Integer.toString(neighbours));
                        button.setText(Integer.toString(neighbours));
                        pepe.setTextColor(button);

                        if( neighbours == 0 ) 
                        {
                            // Show all surrounding cells.
                            button.setBackground(Color.lightGray);
                            button.setText("");
                            findZeroes(x, y);
                        } 
                        else 
                        {
                            button.setBackground(Color.lightGray);
                        }
                    }
                }
            }
            // Right Click
            else if (SwingUtilities.isRightMouseButton(e)) 
            {
                if(tabla.getCells()[x][y].getContent().equals("F")) 
                {   
                    tabla.getCells()[x][y].setContent("");
                    button.setText("");
                    button.setBackground(new Color(0,110,140));

                    //simple blue

                    button.setIcon(pepe.getIconTile());
                    pepe.incMines();
                }
                else if (tabla.getCells()[x][y].getContent().equals("")) 
                {
                    tabla.getCells()[x][y].setContent("F");
                    button.setBackground(Color.blue);	

                    button.setIcon(pepe.getIconFlag());
                    pepe.decMines();
                }
            }

            checkGame();
        }
    }

    //-------------------------RELATED TO SCORES----------------------//


    
    //---------------------EMPTY FUNCTIONS-------------------------------//
    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }    

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
