package circe

import org.junit.Test
import groovy.test.GroovyAssert

class CirceValidateMapTests {

    @Test
    void checkValidationWithErrors() {
        GroovyAssert.assertScript '''
          String name = "john"

          def errors = validate("name", name,
            "error.size":        { it.size() >= 8    },
            "error.startsWith":  { it.startsWith 'z' }
          )

          assert errors.size() == 2
          assert errors.property == ['name', 'name']
          assert errors.keyI18n == ['name.error.size', 'name.error.startsWith']
        '''
    }

    @Test
    void checkValidationWithoutErrors() {
        GroovyAssert.assertScript '''
          String name = "John Nash"

          def errors = validate("name", name,
            "error.size":        { it.size() >= 8    },
            "error.startsWith":  { it.startsWith 'J' }
          )

          assert errors.size() == 0
        '''
    }
}
