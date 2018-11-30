android sqlite database ��ܡ�
    
ʾ����

        SuperSQLite superSQLite = new SuperSQLite(this, "test.db");

        Person data1 = new Person();//����1
        data1.setName("aa");
        data1.setAge(10);
        data1.setPhone("13312345678");

        Person data2 = new Person();//����2
        data2.setName("bb");
        data2.setAge(15);
        data2.setPhone("13412345678");

        superSQLite.insert(Person.class, data1, data2);//�������ݣ�ͬ��SQL��INSERT INTO Person VALUES(10, NULL, 'aa', '13312345678'), (15, NULL, 'bb', '13412345678')

        Log.d(TAG, "onCreate: " + superSQLite.getCount(Person.class));//��ѯ���ݱ�����������ͬ��SQL��SELECT COUNT(*) AS count FROM Person
        Log.d(TAG, "onCreate: " + superSQLite.getCount(Person.class, "Name = 'aa'"));//��ѯ���ݱ��������������������ͬ��SQL��SELECT COUNT(*) AS count FROM Person WHERE Name = 'aa'
        Log.d(TAG, "onCreate: " + superSQLite.query(Person.class, "Name = 'aa'"));//��ѯ���ݱ����ݣ�ͬ��SQL��SELECT * FROM Person WHERE Name = 'aa'
        

@Binding("Person")
public class Person {
    @Binding("ID")
    @PrimaryKey
    @NotNull
    @AutoIncrement
    private int id;
    @Binding("Name")
    @NotNull
    private String name;
    @Binding("Age")
    private int age;
    @Binding("Tel")
    private String phone;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return String.format("{%s, %s, %s, %s}", id, name, age, phone);
    }
}