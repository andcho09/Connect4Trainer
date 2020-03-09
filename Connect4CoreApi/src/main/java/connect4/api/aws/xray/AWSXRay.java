package connect4.api.aws.xray;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.amazonaws.xray.entities.DummySubsegment;
import com.amazonaws.xray.entities.Subsegment;

/**
 * Shim for {@link com.amazonaws.xray.AWSXRay} so that we can disable it in environments where X-Ray is disabled.
 */
public class AWSXRay {

	private static final Logger LOGGER = Logger.getLogger(AWSXRay.class);

	/**
	 * Creates an X-Ray subsegment if X-Ray is enabled, otherwise just invokes the function.
	 * @param <R> object to return
	 * @param name name of the segment
	 * @param function the {@link Function} to instrument
	 * @return the returned object of type R from the function
	 */
	public static <R> R createSubsegment(final String name, final Function<Subsegment, R> function) {
		if (isXRayEnabled()) {
			return com.amazonaws.xray.AWSXRay.createSubsegment(name, function);
		} else {
			return function.apply(new DummySubsegment(null));
		}
	}

	/**
	 * The {@link Consumer} version of {@link #createSubsegment(String, Function)}.
	 * @param name
	 * @param consumer
	 */
	public static void createSubsegment(final String name, final Consumer<Subsegment> consumer) {
		if (isXRayEnabled()) {
			com.amazonaws.xray.AWSXRay.createSubsegment(name, consumer);
		} else {
			consumer.accept(new DummySubsegment(null));
		}
	}

	private static Boolean isXRayEnabled = null;

	/**
	 * Best effort to determine whether X-Ray is enabled or not. Is lazy and will cache the result until next invocation.
	 * @return <code>true</code> if enabled, else <code>false</code>
	 */
	public static boolean isXRayEnabled() {
		// Perhaps this should be "synchronized" but data corruption won't occur if multiple threads call this simultaneously
		if (AWSXRay.isXRayEnabled != null) {
			return AWSXRay.isXRayEnabled;
		}

		/*
		 * Note this relies on a environment variable being set by CloudFormation which in turn controls whether X-Ray is enabled (i.e. this
		 * code doesn't actually inspect the Lambda function to see if it's enabled).
		 */
		AWSXRay.isXRayEnabled = "Active".equals(System.getenv("XRAY_ENABLED"));

		LOGGER.debug("Is X-Ray enabled = " + AWSXRay.isXRayEnabled);
		return AWSXRay.isXRayEnabled;
	}
}
