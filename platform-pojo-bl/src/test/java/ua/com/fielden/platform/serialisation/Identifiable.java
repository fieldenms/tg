package ua.com.fielden.platform.serialisation;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class Identifiable
{
    //        public Identifiable() {
    //            // TODO Auto-generated constructor stub
    //        }

    public int value;

    public Identifiable next;
}