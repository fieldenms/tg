#!/usr/bin/env bash

command grep -l -E --include='*.java' 'implements\s+IRenderingCustomiser<Map<String,\s*Object>>' -R . \
    | xargs sed -E -i \
    -e '10a import ua.com.fielden.platform.web.centre.api.resultset.CssRenderingCustomiser;' \
    -e 's/implements\s+IRenderingCustomiser<Map<String,\s*Object>>/extends CssRenderingCustomiser/g' \
    -e 's/Optional<Map<String,\s*Object>>\s+getCustomRenderingFor/Map<String, Object> getCustomRenderingFor/g'
