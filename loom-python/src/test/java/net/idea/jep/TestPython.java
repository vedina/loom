package net.idea.jep;

import org.junit.Test;

import jep.Interpreter;
import jep.SharedInterpreter;

public class TestPython {
	@Test
	public void testPython() throws Exception {
		try (Interpreter interp = new SharedInterpreter()) {
		    interp.exec("import torch");
		    // any of the following work, these are just pseudo-examples

		    // using exec(String) to invoke methods
		    //interp.set("arg", obj);
		    interp.exec("use_cuda = torch.cuda.is_available()");
		    Object result1 = interp.getValue("use_cuda");
		    System.out.println(result1);
		    // using getValue(String) to invoke methods
		   // Object result2 = interp.getValue("somePyModule.foo2()");

		    // using invoke to invoke methods
		    //interp.exec("foo3 = somePyModule.foo3")
		    //Object result3 = interp.invoke("foo3", obj);
		}
	}

}
