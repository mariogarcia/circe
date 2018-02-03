package circe

import org.junit.Test
import groovy.test.GroovyAssert

class CirceValidateASTTests {

    @Test
    void checkValidationWithErrors() {
        GroovyAssert.assertScript '''
          String name = "john"

          def errors = validate("name", name) { String n ->
            check: 'size'
            n.size() >= 5

            check: 'startsWith'
            n.startsWith 'n'
          }

          assert errors.size() == 2
        '''
    }

    @Test
    void checkValidationWithoutErrors() {
        GroovyAssert.assertScript '''
          String name = "Norman Foster"

          def errors = validate("name", name) { String n ->
            check: 'size'
            n.size() >= 5

            check: 'startsWith'
            n.startsWith 'N'
          }

          assert errors.size() == 0
        '''
    }

    @Test
    void checkErrorKeys() {
        GroovyAssert.assertScript '''
          String name = "john"

          def errors = validate("name", name) { String n ->
            check: 'size'
            n.size() >= 5

            check: 'startsWith'
            n.startsWith 'n'
          }

          assert errors.property == ["name", "name"]
          assert errors.key == ["size", "startsWith"]
          assert errors.keyI18n == ["name.size", "name.startsWith"]
        '''
    }
}
