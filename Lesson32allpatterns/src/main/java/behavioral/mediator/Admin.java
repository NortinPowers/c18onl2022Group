package behavioral.mediator;

public class Admin implements behavioral.mediator.User {

    private behavioral.mediator.Chat chat;
    private String name;

    public Admin(behavioral.mediator.Chat chat, String name) {
        this.chat = chat;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void sendMessage(String message) {
        chat.sendMessage(message, this);
    }

    @Override
    public void getMessage(String message) {
        System.out.println(this.name + " received message: " + message);
    }
}