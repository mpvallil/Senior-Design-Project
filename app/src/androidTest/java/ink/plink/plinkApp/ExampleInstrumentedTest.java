package ink.plink.plinkApp;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.v4.content.ContextCompat.startActivity;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("ink.plink.plinkApp", appContext.getPackageName());
    }

    @Test
    public void testHome() {
        startActivity(InstrumentationRegistry.getContext(), new Intent(InstrumentationRegistry.getContext(), SplashActivity.class), null);
    }
}
