package se.spacify.ui.render.jexer;

import se.spacify.controls.Button;
import se.spacify.controls.Label;
import se.spacify.controls.Panel;
import se.spacify.controls.TextField;

/**
 * A minimal, hand-built root view demonstrating the {@link se.spacify.controls}
 * abstraction working end-to-end under the Jexer backend: a label, a text
 * field and a button, wired together entirely through the same
 * backend-neutral {@code Control} API the production Swing UI uses. Typing in
 * the field and pressing the button proves bilateral sync in both directions
 * — the field's native peer feeds {@link TextField#getText()}, and the
 * label's native peer updates from a plain {@link Label#setText}.
 *
 * <p>The production Chrome (see {@code se.spacify.ui.chrome}) depends on
 * controls not yet ported to this abstraction (tables, trees, split panes,
 * toolbars), so it is Swing-only; this view stands in for it when the Jexer
 * backend is active.
 */
public class JexerHomeView extends Panel {

	public JexerHomeView() {
		super(Axis.VERTICAL);

		Label greeting = new Label("Type your name and press the button:");
		TextField name = new TextField("");
		Button sayHello = new Button("Say hello");

		add(greeting);
		add(name);
		add(sayHello);

		sayHello.addActionListener(() -> {
			String who = name.getText();
			greeting.setText(who == null || who.isBlank() ? "Hello!" : "Hello, " + who + "!");
		});
	}
}
