android sqlite database ��ܡ�

�ŵ㣺

�����ʹ���������ķ�ʽ������ android SQLiteDatabase���ݿ⣬ʹ�����ݿ�Ĳ������׶�������ʹ�ö������������ݿ��һ�С�

֧���Զ����ṹ����Ҫ�������� String[]COLUMNS_NAME����ṹ�ֶ������������ݱ��������磺

private static final COLUMNS_NAME = {"name", "sex", "age"};

ȱ�㣺

ֻ֧�ֵ������ݿ⡣

���ɷ�����
��Ϊģ����ӻ���AAR�����

repositories {

    flatDir {
    
        dirs 'libs' //this way we can find the .aar file in libs folder
        
    }
    
}


    implementation(name: 'supersqlite-20180806', ext: 'aar')
    
    
ʾ����

//����ģ�Ͷ��壬����ģ�ͱ����� BaseRow������

public class DataRow extends BaseRow {

    //�Զ������ݱ�ṹ
    
    private static final COLUMNS_NAME = {"name", "sex", "age", "tel", "createTime"};
    
    String name;
    
    boolean sex;
    
    int age;
    
    String tel;
    
    Date createTime;
    
}

SuperSQLite<DataRow> superSQLite = new SuperSQLite<>(context, "database.db", DataRow.class);

superSQLite.insertOrUpdate(new DataRow());

superSQLite.query(new int[]{1});

��ӭһ��Ľ�ѧϰ��������ϵ��final.hsx@qq.com

