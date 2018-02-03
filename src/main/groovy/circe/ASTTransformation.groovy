package circe

import static asteroid.Phase.GLOBAL

import groovy.transform.CompileStatic

import asteroid.Phase
import asteroid.AbstractGlobalTransformation
import asteroid.transformer.Transformer

/**
 * Global transformation responsible for transforming validation calls
 * to Helios validation calls
 *
 * @since 0.1.0
 */
@CompileStatic
@Phase(GLOBAL.CONVERSION)
class ASTTransformation extends AbstractGlobalTransformation {

    @Override
    List<Class<Transformer>> getTransformers() {
        return [ValidateMethodCallTransformer]
    }
}
