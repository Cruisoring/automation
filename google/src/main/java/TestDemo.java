import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class TestDemo {
    public static int count = 10;

    interface MyInt {
        void m1();
    }



    public static class Parent {
        public void thePublic(){
            System.out.print("Super_Pub");
        }

        protected void theProtected(){}{
            System.out.print("SUper_Pro");
        }
    }

    public static class Child extends Parent {
        public void thePublic(){
            System.out.print("Child_pub");
        }

        protected void theProtected(){}{
            System.out.print("Child_pro");
        }
    }

    public static void main(String[] args)  {
//        System.out.println(String.format("%tT", Calendar.getInstance()));
        Long i = new Long(10);
        long j = 10;
        long k= -5;

        boolean result = Long.compare(i, k)>0;
        result = Long.compareUnsigned(i, k) > 0;

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2000, Calendar.AUGUST, 30);
            calendar.roll(Calendar.MONTH, 7);
            System.out.println(calendar.get(Calendar.DATE));
            System.out.println(calendar.get(Calendar.MONTH));
            System.out.println(calendar.get(Calendar.YEAR));

            LocalDate localDate = LocalDate.parse("2012-01-15", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            System.out.print(localDate.getDayOfMonth() +", "+ localDate.getMonthValue());
        } catch (Exception e) {
            e.printStackTrace();
        }


        new Child().thePublic();
        new TestDemo().count = 20;
    }
}
