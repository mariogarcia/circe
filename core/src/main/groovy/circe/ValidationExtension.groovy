package circe

import helios.ValidatorError

/**
 * List<ValidatorError> errors = validate(person) { Person p ->
 *   check: 'age.underage'
 *   p.age == 2
 *
 *   check: 'size.too.long'
 *   p.size >= 1000
 *
 *   check: 'size.too.short'
 *   p.size <= 0
 * }
 *
 * @since 0.1.0
 */
class ValidationExtension {

    /**
     * @param thisObject
     * @param subject
     * @param validation
     * @return
     * @since 0.1.0
     */
    static <T> List<ValidatorError> validate(Object thisObject, T subject, Closure<List<ValidatorError>> validation) {
        return null
    }
}
