package connect4.api.json;

import java.io.Serializable;

/**
 * Instructs the service to warm itself up because Lambda cold starts are expensive.
 */
public class WarmRequest implements Serializable {

	private static final long serialVersionUID = 1L;
}
