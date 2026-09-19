/* 上海如静知华信息科技有限公司 https://www.zhuatech.cn/ */
package cn.zhuatech.rebate;
import org.springframework.stereotype.Component;
import java.util.*;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import static cn.zhuatech.rebate.Model.*;
import static cn.zhuatech.rebate.Engine.*;
/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@Component public class Domain {
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static Map<String,Object> copy(Row r){return new LinkedHashMap<>(r.data());}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal n(Row r,String k){return num(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal z(Map<String,Object>d,String k){return d.containsKey(k)?num(d,k):BigDecimal.ZERO;}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static String t(Row r,String k){return txt(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static List<Row> linked(Engine e,User u,String module,String key,String id){return e.all(u,module).stream().filter(r->t(r,key).equals(id)).toList();}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void unique(Engine e,User u,String module,Map<String,Object>d,String key){require(e.all(u,module).stream().noneMatch(r->t(r,key).equalsIgnoreCase(txt(d,key))),"重复的"+key);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void dates(Map<String,Object>d,String from,String to){require(!date(d,to).isBefore(date(d,from)),"结束日期不能早于开始日期");}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void change(Engine e,User u,Row row,String state,Map<String,Object>d,String note){e.save(u,row,state,d,"LINKED",note);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void edit(Engine e,User u,Row r,Map<String,Object>d){
  if(r.module().equals("readings")){require(e.ref(u,r.data(),"job","jobs").state().equals("RUNNING")&&txt(d,"job").equals(t(r,"job")),"仅进行中的任务可以修改测量值，且不得迁移任务");require(linked(e,u,"readings","job",t(r,"job")).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"point").equals(txt(d,"point"))),"测量点编号重复");return;}
  if(r.module().equals("versions")){require(e.ref(u,r.data(),"artwork","artworks").state().equals("DRAFT"),"已送审稿件不可修改");require(txt(d,"artwork").equals(t(r,"artwork"))&&num(d,"revision").compareTo(n(r,"revision"))==0,"版本不能迁移任务或改写版本号");return;}
  for(var m:e.spec().modules())for(Row other:e.all(u,m.key()))if(!other.id().equals(r.id())&&other.data().values().stream().anyMatch(v->r.id().equals(v)))throw new Failure(409,"资料已有下游引用，请新建版本而不是改写历史");
  var fields=e.spec().module(r.module()).fields().stream().map(Field::key).toList();
  r.data().forEach((k,v)->{if(!fields.contains(k))d.put(k,v);});
  if(d.containsKey("start")&&d.containsKey("end"))dates(d,"start","end");
  if(d.containsKey("from")&&d.containsKey("to"))dates(d,"from","to");
  for(String key:List.of("serial","sku","invoice","invoiceNo","lockNo"))if(d.containsKey(key))require(e.all(u,r.module()).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,key).equalsIgnoreCase(txt(d,key))),"重复唯一业务标识: "+key);
  if(d.containsKey("bonusRate"))require(num(d,"bonusRate").compareTo(num(d,"baseRate"))>=0,"达档返利率不能低于基础返利率");
  if(d.containsKey("lifeLimit"))require(num(d,"serviceEvery").compareTo(num(d,"lifeLimit"))<=0,"保养间隔不能大于寿命");
  if(d.containsKey("defects"))require(num(d,"defects").compareTo(num(d,"shots"))<=0,"不良数不能超过生产次数");
  if(d.containsKey("nps"))require(num(d,"nps").compareTo(BigDecimal.TEN)<=0&&num(d,"csat").compareTo(new BigDecimal("5"))<=0,"评价分数超出范围");
  if(d.containsKey("oxygenMin"))require(num(d,"oxygenMin").compareTo(num(d,"oxygenMax"))<0,"氧气下限须小于上限");
  if(r.module().equals("invoices"))require(e.all(u,"invoices").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"shipment").equals(txt(d,"shipment"))),"运单已关联结算账单");
  if(r.module().equals("sales")){Row program=e.ref(u,d,"program","programs");require(program.state().equals("ACTIVE")&&!date(d,"soldAt").isBefore(date(program.data(),"start"))&&!date(d,"soldAt").isAfter(date(program.data(),"end")),"协议状态或销售日期无效");}
  if(r.module().equals("jobs")){Row instrument=e.ref(u,d,"instrument","instruments"),standard=e.ref(u,d,"standard","standards");require(!instrument.state().equals("RETIRED")&&t(instrument,"unit").equals(t(standard,"unit")),"器具状态或计量单位无效");require(!date(d,"performedAt").isAfter(LocalDate.now()),"不能记录未来校准");}
  if(r.module().equals("permits")){require(ChronoUnit.DAYS.between(date(d,"start"),date(d,"end"))<=7,"许可最长七天");require(t(e.ref(u,d,"isolation","isolations"),"location").equals(txt(d,"location")),"隔离区域不匹配");}
  if(r.module().equals("responses")){require(e.all(u,"responses").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"survey").equals(txt(d,"survey"))&&t(x,"customer").equals(txt(d,"customer"))),"客户已存在该问卷反馈");require(t(e.ref(u,d,"customer","customers"),"consent").equals("YES"),"客户未允许反馈邀请");}
  if(r.module().equals("products")){String barcode=txt(d,"barcode");require(barcode.matches("\\d{13}"),"条码须为 EAN-13");int sum=0;for(int x=0;x<12;x++)sum+=(barcode.charAt(x)-'0')*(x%2==0?1:3);require((10-sum%10)%10==barcode.charAt(12)-'0',"EAN-13 校验位不正确");}

 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public Map<String,Object> metrics(Engine e,User u){
  var out=new LinkedHashMap<String,Object>();out.put("生效协议",e.all(u,"programs").stream().filter(r->r.state().equals("ACTIVE")).count());out.put("应付返利",e.all(u,"settlements").stream().filter(r->r.state().equals("APPROVED")).map(r->n(r,"rebate")).reduce(BigDecimal.ZERO,BigDecimal::add));;return out;
 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void create(Engine e,User u,String module,Map<String,Object>d){switch(module){case "programs" -> {dates(d,"start","end");require(num(d,"bonusRate").compareTo(num(d,"baseRate"))>=0,"达档返利率不能低于基础返利率");}
case "sales" -> {Row p=e.ref(u,d,"program","programs");require(p.state().equals("ACTIVE"),"协议未生效");LocalDate sold=date(d,"soldAt");require(!sold.isBefore(date(p.data(),"start"))&&!sold.isAfter(date(p.data(),"end")),"销售日期超出协议期限");unique(e,u,module,d,"invoice");d.put("returned",0);d.put("collected",0);}
case "settlements" -> dates(d,"from","to"); default -> {} }}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public String action(Engine e,User u,Row r,String action,Map<String,Object>i,Map<String,Object>d){
  String k=r.module()+"."+action;switch(k){
case "sales.collect" -> {require(num(i,"amount").compareTo(num(d,"amount"))==0,"回款核销须与销售金额一致");d.put("collected",num(i,"amount"));}
case "sales.return" -> {require(txt(d,"settlement").isEmpty(),"已锁定返利结算的销售不可退货");BigDecimal total=z(d,"returned").add(num(i,"amount"));require(total.compareTo(z(d,"collected"))<=0,"销退金额超过实收");d.put("returned",total);}
case "settlements.calculate" -> {
 Row program=e.ref(u,d,"program","programs");require(Set.of("ACTIVE","RETIRED").contains(program.state()),"协议尚未审批");
 List<Row> sales=e.all(u,"sales").stream().filter(s->s.state().equals("PAID")&&t(s,"partner").equals(txt(d,"partner"))&&t(s,"program").equals(program.id())&&t(s,"settlement").isEmpty()&&!date(s.data(),"soldAt").isBefore(date(d,"from"))&&!date(s.data(),"soldAt").isAfter(date(d,"to"))).toList();
 require(!sales.isEmpty(),"期间没有可结算销售");
 BigDecimal net=sales.stream().map(s->n(s,"collected").subtract(n(s,"returned"))).reduce(BigDecimal.ZERO,BigDecimal::add);require(net.signum()>0,"净销售额必须大于零");
 BigDecimal rate=net.compareTo(n(program,"threshold"))>=0?n(program,"bonusRate"):n(program,"baseRate");BigDecimal total=BigDecimal.ZERO;
 for(Row sale:sales){BigDecimal value=n(sale,"collected").subtract(n(sale,"returned")),rebate=money(value.multiply(rate).divide(new BigDecimal("100")));total=total.add(rebate);var sd=copy(sale);sd.put("settlement",r.id());change(e,u,sale,sale.state(),sd,"返利结算占用");
 e.ledger(u,"entries","POSTED",Map.of("settlement",r.id(),"sale",sale.id(),"net",value,"rate",rate,"rebate",rebate));}
 d.put("netSales",net);d.put("rate",rate);d.put("rebate",total);d.put("saleCount",sales.size());
}
case "settlements.reject" -> {
 for(Row sale:linked(e,u,"sales","settlement",r.id())){var sd=copy(sale);sd.remove("settlement");change(e,u,sale,sale.state(),sd,"返利驳回释放");}
 for(Row entry:linked(e,u,"entries","settlement",r.id()))if(entry.state().equals("POSTED"))change(e,u,entry,"VOID",copy(entry),"返利驳回冲销");
 d.remove("netSales");d.remove("rate");d.remove("rebate");d.remove("saleCount");
}
case "settlements.pay" -> {require(e.all(u,"settlements").stream().noneMatch(s->t(s,"reference").equals(txt(i,"reference"))),"付款凭证重复");d.putAll(i);}
 default -> {} }return null;
 }
}
