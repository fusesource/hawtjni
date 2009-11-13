package test;

import static org.junit.Assert.*;
import static test.Example.*;

import org.junit.Test;

import test.Example.foo;

public class ExampleTest {

    @Test
    public void test() {
        // Allocate and initialize some memory on the heap.
        long ptr = malloc(foo.SIZEOF);
        memset(ptr, 0, foo.SIZEOF);

        // Configure an object that can be mapped to a C structure.
        foo expected = new foo();
        expected.a = 35;
        expected.b = Integer.MAX_VALUE;
        
        System.arraycopy("Hello World!".getBytes(), 0, expected.c, 0, 5);
        
        // Marshal the object to the allocated heap memory
        memmove(ptr, expected, foo.SIZEOF);
        
        // Unmarshal the object from the allocated heap memory.
        foo acutal = new foo();
        memmove(acutal, ptr, foo.SIZEOF);
        
        assertEquals(expected, acutal); 
        
        // Heap memory is not GCed, we must manually free it.
        free(ptr);
    }
    
}
