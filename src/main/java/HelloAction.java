import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.Topic;

import javax.naming.Context;


public class HelloAction extends AnAction {

    Project project;
    MessageBus myBus;

    public HelloAction() {
        super("Hello");
    }

    public void actionPerformed(AnActionEvent event) {
        // Clicked the action in the menu.
        project = event.getProject();
        myBus = project.getMessageBus();
        Messages.showMessageDialog(project, "HelloWorld", "Greeting", Messages.getInformationIcon());
    }


    // TODO: Explore and use Topics
    // Defining business interface and topic
    public interface ChangeActionNotifier {

        Topic<ChangeActionNotifier> CHANGE_ACTION_TOPIC = Topic.create("custom name", ChangeActionNotifier.class);

        void beforeAction(Context context);
        void afterAction(Context context);
    }


    public HelloAction(MessageBus bus) {
        bus.connect().subscribe(ChangeActionNotifier.CHANGE_ACTION_TOPIC, new ChangeActionNotifier() {
            @Override
            public void beforeAction(Context context) {
                // Process 'before action' event.
            }
            @Override
            public void afterAction(Context context) {
                // Process 'after action' event.
            }
        });
    }

    public void doChange(Context context) {
        ChangeActionNotifier publisher = myBus.syncPublisher(ChangeActionNotifier.CHANGE_ACTION_TOPIC);
        publisher.beforeAction(context);
        try {
            // Do action
            // ...
        } finally {
            publisher.afterAction(context);
        }
    }
}

