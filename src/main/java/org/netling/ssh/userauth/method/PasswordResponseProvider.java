/*
 * Copyright 2010 netling project <http://netling.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netling.ssh.userauth.method;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.netling.ssh.userauth.password.PasswordFinder;
import org.netling.ssh.userauth.password.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordResponseProvider
        implements ChallengeResponseProvider {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final char[] EMPTY_RESPONSE = new char[0];

    private static final Collection<String> DEFAULT_ACCEPTABLE_PROMPTS =
            Collections.unmodifiableCollection(Arrays.asList("Password:"));

    private final Collection<String> acceptablePrompts;
    private final PasswordFinder pwdf;
    private Resource resource;
    private boolean gaveAlready;

    public PasswordResponseProvider(PasswordFinder pwdf) {
        this(pwdf, DEFAULT_ACCEPTABLE_PROMPTS);
    }

    public PasswordResponseProvider(PasswordFinder pwdf, Collection<String> acceptablePrompts) {
        this.pwdf = pwdf;
        this.acceptablePrompts = acceptablePrompts;
    }

    @Override
    public List<String> getSubmethods() {
        return Collections.emptyList();
    }

    @Override
    public void init(Resource resource, String name, String instruction) {
        this.resource = resource;
        log.debug("Challenge - name=`{}`; instruction=`{}`", name, instruction);
    }

    @Override
    public char[] getResponse(String prompt, boolean echo) {
        if (!gaveAlready && !echo && acceptablePrompts.contains(prompt)) {
            gaveAlready = true;
            return pwdf.reqPassword(resource);
        }
        return EMPTY_RESPONSE;
    }

    @Override
    public boolean shouldRetry() {
        return pwdf.shouldRetry(resource);
    }

}
