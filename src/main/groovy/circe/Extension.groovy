package circe

import static helios.ValidatorError.error
import static helios.ValidatorError.errors

import groovy.transform.CompileStatic
import helios.Helios
import helios.Validator
import helios.ValidatorError

/**
 * The Circe extension will make available a certain type of
 * validation call that eventually will be transformed thanks to the
 * {@link ASTTransformation} class to a valid Helios validation
 *
 * Specially useful when using an IDE because the IDE will make the
 * extension method available to be called.
 *
 * @since 0.1.0
 */
@CompileStatic
class Extension {

    /**
     * This method will be available anywhere in your code and will
     * make it possible to validate a given object using the following
     * sugar syntax.
     *
     * <pre><code>
     * List<ValidatorError> errors = validate('person', person) { Person p ->
     *   check: 'age.underage'
     *   p.age == 2
     *
     *   check: 'size.too.long'
     *   p.size >= 1000
     *
     *   check: 'size.too.short'
     *   p.size <= 0
     * }
     * </code></pre>
     *
     * @param thisObject means this method extension will be available in any object
     * @param key the key of the validation message key
     * @param subject the object to validate
     * @param validation the dsl used to validate the object
     * @return a list of {@link ValidatorError} if any validation
     * error has been found or an empty list otherwise
     * @since 0.1.0
     */
    static <T> List<ValidatorError> validate(Object thisObject, String key, T subject, Closure<List<ValidatorError>> validation) {
        return null
    }

    /**
     * <pre><code>
     * validate('name', 'John Grisham',
     *   'error.size':   { it.size() >= 19 },
     *   'error.starts': { it.startsWith 'John' }
     * )
     * </code></pre>
     *
     * @param thisObject
     * @param key
     * @param subject
     * @param validators
     * @return
     * @since 0.1.0
     */
    static <T> List<ValidatorError> validate(Object thisObject, Map<String, Closure<Boolean>> checks, String key, T subject) {
        return Helios.validate(key, subject, checks.collect(Extension.&mapCheckToValidator) as List<Validator<T>>)
    }

    private static <T> Validator<T> mapCheckToValidator(String errorKey, Closure<Boolean> check) {
        return { T subject -> check(subject) ?
                errors() :
                errors(error(subject, errorKey))
        } as Validator<T>
    }
}
