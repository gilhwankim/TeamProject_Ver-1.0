package makeSound;

import java.io.FileInputStream;

import javazoom.jl.player.Player;

public class MakeSound {
   
   public static void kitchenOrderSound() {
      try {
         FileInputStream fis = new FileInputStream("src\\mp3\\bell.mp3"); //���� ���� ���
         Player player = new Player(fis);
         player.play(); //�÷��̾� ����
      }catch (Exception e) {}
   }      
}