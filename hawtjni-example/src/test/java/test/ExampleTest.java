package test;

import static org.junit.Assert.*;
import static test.Example.*;

import org.fusesource.hawtjni.runtime.Callback;
import org.fusesource.hawtjni.runtime.JNIEnv;
import org.junit.Test;
import static org.fusesource.hawtjni.runtime.PointerMath.*;

import test.Example.bar;

public class ExampleTest {

    static private int staticCallbackResult;
    private int instanceCallbackResult;

    @Test
    public void testPointerMath() {
        long values[] = new long[]{
                0, Long.MAX_VALUE, Long.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE,
                0+1, Long.MAX_VALUE+1, Long.MIN_VALUE+1, Integer.MAX_VALUE+1, Integer.MIN_VALUE+1,
                0-1, Long.MAX_VALUE-1, Long.MIN_VALUE-1, Integer.MAX_VALUE-1, Integer.MIN_VALUE-1};
        for( long i: values ) {
           assertEquals(char_add(i, 1), add(i, 1) );
           assertEquals(char_add(i, -1), add(i, -1) );
        }
    }

    @Test
    public void test() {
        // Allocate and initialize some memory on the heap.
        long ptr = malloc(bar.SIZEOF);
        memset(ptr, 0, bar.SIZEOF);

        // Configure an object that can be mapped to a C structure.
        bar expected = new bar();
        expected.a = 35;
        expected.b = Integer.MAX_VALUE;
        
        System.arraycopy("Hello World!".getBytes(), 0, expected.c, 0, 5);
        
        // Marshal the object to the allocated heap memory
        memmove(ptr, expected, bar.SIZEOF);
        
        // Unmarshal the object from the allocated heap memory.
        bar acutal = new bar();
        memmove(acutal, ptr, bar.SIZEOF);
        
        assertEquals(expected, acutal); 
     
        Callback callback = new Callback(this, "instanceCallback", 1);
        callmeback(callback.getAddress());
        assertEquals(69, instanceCallbackResult);
        callback.dispose();

        long r1 = Range.Range();
        Range.dump(r1);

        long r2 = Range.Range(10,100);
        Range.dump(r2);

        Range.delete(r1);
        Range.delete(r2);


        callback = new Callback(ExampleTest.class, "staticCallback", 1);
        callmeback(callback.getAddress());
        assertEquals(69, staticCallbackResult);
        callback.dispose();
        
        // Heap memory is not GCed, we must manually free it.
        free(ptr);

        passingtheenv("Hiram", null);
    }
    
    public long instanceCallback(long value) {
        this.instanceCallbackResult = (int) value;
        return 0;
    }
    
    static public long staticCallback(long value) {
        staticCallbackResult = (int) value;
        return 0;
    }

}
