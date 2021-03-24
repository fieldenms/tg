package ua.com.fielden.platform.eql.stage1.sources;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.stage1.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnVoid;

public class Source1BasedOnVoid  implements ISource1<Source2BasedOnVoid> {

    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        throw new EqlStage1ProcessingException("This method shouldn't be invoked.");    
    }
    
    @Override
    public String getAlias() {
        throw new EqlStage1ProcessingException("This method shouldn't be invoked.");
    } 

    @Override
    public Source2BasedOnVoid transform(final PropsResolutionContext context) {
        return new Source2BasedOnVoid();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = 1;
        return prime * result + Source1BasedOnVoid.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof Source1BasedOnVoid;
    }
}