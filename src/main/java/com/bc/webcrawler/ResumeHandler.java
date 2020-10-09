package com.bc.webcrawler;

import java.util.Collection;
import java.util.Collections;

public interface ResumeHandler {
    
    ResumeHandler NO_OP = new ResumeHandler() {
        @Override
        public boolean isExisting(String name) {
            return false;
        }
        @Override
        public boolean saveIfNotExists(String name) {
            return false;
        }
        @Override
        public void save(Collection<String> name) { }
        @Override
        public Collection<String> load() {
            return Collections.EMPTY_LIST;
        }
    };
    
    boolean isExisting(String name);
  
    boolean saveIfNotExists(String name);
    
    void save(Collection<String> names);
    
    Collection<String> load();
}
