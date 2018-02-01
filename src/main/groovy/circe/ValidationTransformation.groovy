package circe

import static asteroid.Phase.GLOBAL

import groovy.transform.CompileStatic

import asteroid.Phase
import asteroid.AbstractGlobalTransformation
import asteroid.transformer.Transformer

/**
 * @since 0.1.0
 */
@CompileStatic
@Phase(GLOBAL.CONVERSION)
class ValidationTransformation extends AbstractGlobalTransformation {

    @Override
    List<Class<Transformer>> getTransformers() {
        return [ClosureTransformer]
    }
}
