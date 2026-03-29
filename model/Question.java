package model;
public class Question {
    public int id;
    public String text;
    public String a, b, c, d;
    public String correct;

    public Question(int id, String text, String a, String b, String c, String d, String correct){
        this.id = id;
        this.text = text;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.correct = correct;
    }
}
