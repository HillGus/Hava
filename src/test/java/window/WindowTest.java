package window;

import hava.window.Window;
import hava.window.components.builders.ButtonPanelBuilder;

public class WindowTest {

  
  public static void main(String args[]) {
    
    Window window = new Window("Teste", 500, 500);
    
    window.setVisible(true);
    
    window.addMainPanel(new ButtonPanelBuilder()
                          .name("Principal")
                          .gotoButton("Segundo")
                          .switchButton("Teste1", "Teste2", 
                              () -> System.out.println("Teste1"), 
                              () -> System.out.println("Teste2"))
                          .actionButton("Teste3", () -> System.out.println("Teste3"))
                          .centralize()
                          .padding(5)
                          .size(500, 500)
                          .build());
    
    window.addPanel(new ButtonPanelBuilder()
                      .name("Segundo")
                      .backButton("Voltar")
                      .size(100, 100)
                      .centralize()
                      .build());
    
    window.repaint();
  }
}