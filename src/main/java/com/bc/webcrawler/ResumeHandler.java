package com.bc.webcrawler;

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
    };
    
    boolean isExisting(String name);
  
    boolean saveIfNotExists(String name);
}
