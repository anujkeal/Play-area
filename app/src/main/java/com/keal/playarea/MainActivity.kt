package com.keal.playarea

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color as AColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Red=Color(0xFFC8102E); private val Gold=Color(0xFFD6A84F); private val Cream=Color(0xFFFFF7EC); private val Ink=Color(0xFF30251F); private val Muted=Color(0xFF887A70)
private data class Shirt(val name:String,val hex:String){val color:Color get()=Color(AColor.parseColor(hex))}
private val shirts=listOf("Red:#E53935","Maroon:#800000","Orange:#F57C00","Peach:#FFAB91","Yellow:#FFEB3B","Mustard Yellow:#D6A21E","Lime:#C0CA33","Green:#2E7D32","Olive:#737B25","Mint:#80CBC4","Teal:#00897B","Sky Blue:#4FC3F7","Blue:#1E88E5","Navy:#172A53","Purple:#7E57C2","Lavender:#B39DDB","Pink:#EC407A","Hot Pink:#D81B60","White:#FFFFFF","Black:#242424","Grey:#9E9E9E","Brown:#795548","Beige:#D7C3A5","Turquoise:#26A69A").map{val p=it.split(":");Shirt(p[0],p[1])}
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);SessionAlarmReceiver.ensureChannel(this);setContent{PlayTheme{Home(
  start={s->SessionStore.add(this,s);SessionAlarmReceiver.schedule(this,s)},
  end={id->SessionAlarmReceiver.cancel(this,id);SessionStore.finish(this,id)}
 )}};if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),7)}
}
@Composable private fun PlayTheme(content: @Composable () -> Unit){MaterialTheme(colorScheme=lightColorScheme(primary=Red,secondary=Gold,background=Cream,surface=Color.White,onBackground=Ink,onSurface=Ink),content=content)}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun Home(start:(PlaySession)->Unit,end:(Long)->Unit){
 val ctx=androidx.compose.ui.platform.LocalContext.current;var data by remember{mutableStateOf(SessionStore.all(ctx))};var now by remember{mutableLongStateOf(System.currentTimeMillis())};var tab by remember{mutableStateOf(false)};var query by remember{mutableStateOf("")};var sheet by remember{mutableStateOf(false)}
 LaunchedEffect(Unit){while(true){now=System.currentTimeMillis();data=SessionStore.all(ctx);delay(1000)}}
 val active=data.filter{it.finishedAt==null}.sortedBy{it.endsAt-now};val previous=data.filter{it.finishedAt!=null}.sortedByDescending{it.finishedAt}
 Scaffold(containerColor=Cream,floatingActionButton={FloatingActionButton(onClick={sheet=true},containerColor=Red,contentColor=Color.White){Icon(Icons.Default.Add,"Add child")}}){pad->
 Column(Modifier.fillMaxSize().padding(pad).padding(horizontal=18.dp)){
  Spacer(Modifier.height(22.dp));Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("PLAY AREA",color=Red,fontSize=12.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp);Text("Little moments,\nbig adventures.",fontFamily=FontFamily.Serif,fontSize=28.sp,fontWeight=FontWeight.Bold,lineHeight=32.sp)};Surface(shape=RoundedCornerShape(16.dp),color=Color.White){Column(Modifier.padding(14.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("${active.size}",fontSize=24.sp,color=Red,fontWeight=FontWeight.Bold);Text("ACTIVE",fontSize=10.sp,color=Muted)}}}
  Spacer(Modifier.height(18.dp));OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Search name or t-shirt color")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true,shape=RoundedCornerShape(14.dp))
  Spacer(Modifier.height(12.dp));Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFEFE4D7)).padding(4.dp)){listOf(false,true).forEach{v->Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp)).background(if(tab==v)Color.White else Color.Transparent).clickable{tab=v},Alignment.Center){Text(if(v)"Previous (${previous.size})" else "Active (${active.size})",color=if(tab==v)Red else Muted,fontWeight=FontWeight.Bold)}}}
  val list=if(tab)previous else active;val filtered=list.filter{it.name.contains(query,true)||it.colorName.contains(query,true)}
  if(filtered.isEmpty()){Column(Modifier.fillMaxWidth().padding(top=65.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("✦",fontSize=42.sp,color=Gold);Text(if(tab)"No previous sessions yet" else if(query.isBlank())"Ready for some fun?" else "No matching sessions",fontFamily=FontFamily.Serif,fontSize=21.sp,fontWeight=FontWeight.Bold);Text(if(tab)"Completed sessions appear here." else "Tap + when a child is ready to play.",color=Muted,fontSize=13.sp)}}
  else LazyColumn(verticalArrangement=Arrangement.spacedBy(11.dp),contentPadding=PaddingValues(top=12.dp,bottom=90.dp)){items(filtered, key = { it.id }){s->if(tab) PreviousCard(s) else ActiveCard(s,now){end(s.id)}}}
 }
 }
 if(sheet) AddSheet({sheet=false}){name,color,pattern->val t=System.currentTimeMillis();start(PlaySession(t,name.trim(),color.name,color.hex,pattern,t,t+1_800_000L));data=SessionStore.all(ctx);sheet=false}
}
@Composable private fun ActiveCard(s:PlaySession,now:Long,onEnd:()->Unit){
 val left=s.endsAt-now;val over=left<=0;val c=when{over||left<120000->Color(0xFFC62828);left<600000->Color(0xFFEF7D22);else->Color(0xFF2E7D50)}
 Card(Modifier.fillMaxWidth().alpha(if(over)0.88f else 1f),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=if(over)Color(0xFFFFE5E2) else Color.White),border=androidx.compose.foundation.BorderStroke(1.dp,if(over)Color.Red else Color(0xFFEDE1D3))){Column(Modifier.padding(14.dp)){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(46.dp).clip(CircleShape).background(hexColor(s.colorHex)),Alignment.Center){Text(s.name.take(1).uppercase(),color=Color.White,fontWeight=FontWeight.Bold)};Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(s.name,fontWeight=FontWeight.Bold,fontSize=17.sp);Text("${s.colorName} • ${s.pattern}",fontSize=12.sp,color=Muted)};Column(horizontalAlignment=Alignment.End){Text(if(over)"OVERTIME" else "TIME LEFT",fontSize=9.sp,color=c,fontWeight=FontWeight.Bold);Text(if(over)"+${duration(-left)}" else duration(left),fontSize=21.sp,color=c,fontWeight=FontWeight.Bold)}}
 Spacer(Modifier.height(10.dp));HorizontalDivider(color=Color(0xFFF0E8DF));Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Started ${clock(s.startedAt)}",fontSize=12.sp,color=Muted);Text("Ends ${clock(s.endsAt)}",fontSize=12.sp,color=Muted)};Button(onClick=onEnd,shape=RoundedCornerShape(10.dp),modifier=Modifier.height(46.dp)){Text("End Session")}}
 }}
}
@Composable private fun PreviousCard(s:PlaySession){val finish=s.finishedAt?:s.endsAt;Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(12.dp).clip(CircleShape).background(hexColor(s.colorHex)));Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(s.name,fontWeight=FontWeight.Bold);Text("${s.colorName} • ${date(s.startedAt)}",fontSize=12.sp,color=Muted);Text("${clock(s.startedAt)} – ${clock(finish)}",fontSize=12.sp,color=Muted)};Text("${((finish-s.startedAt).coerceAtLeast(0)/60000)} min",color=Red,fontWeight=FontWeight.Bold)}}}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AddSheet(dismiss:()->Unit,begin:(String,Shirt,String)->Unit){
 var name by remember{mutableStateOf("")};var chosen by remember{mutableStateOf<Shirt?>(null)};var pattern by remember{mutableStateOf("Plain")}
 ModalBottomSheet(onDismissRequest=dismiss,containerColor=Cream){Column(Modifier.fillMaxWidth().padding(horizontal=18.dp).padding(bottom=24.dp)){Text("New play session",fontFamily=FontFamily.Serif,fontSize=25.sp,fontWeight=FontWeight.Bold);Text("A little play goes a long way.",color=Muted,fontSize=13.sp);Spacer(Modifier.height(14.dp));Text("CHILD'S NAME",fontSize=11.sp,fontWeight=FontWeight.Bold,color=Muted);OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),placeholder={Text("Enter child's name")},singleLine=true,keyboardOptions=KeyboardOptions(capitalization=KeyboardCapitalization.Words));Spacer(Modifier.height(12.dp));Text("T-SHIRT COLOR · REQUIRED",fontSize=11.sp,fontWeight=FontWeight.Bold,color=Muted);Spacer(Modifier.height(8.dp))
 androidx.compose.foundation.lazy.grid.LazyVerticalGrid(columns=androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),modifier=Modifier.height(220.dp),horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){items(count=shirts.size){index->val c=shirts[index];Surface(Modifier.fillMaxWidth().height(42.dp).clickable{chosen=c},shape=RoundedCornerShape(9.dp),color=Color.White,border=androidx.compose.foundation.BorderStroke(if(chosen==c)2.dp else 1.dp,if(chosen==c)Red else Color.LightGray)){Row(Modifier.padding(horizontal=6.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(16.dp).clip(CircleShape).background(c.color));Spacer(Modifier.width(5.dp));Text(c.name,fontSize=10.sp,maxLines=1);if(chosen==c)Icon(Icons.Default.Check,null,tint=Red,modifier=Modifier.size(13.dp))}}}}
 chosen?.let{Text("✓ Selected: ${it.name}",color=Red,fontWeight=FontWeight.Bold,fontSize=13.sp)}
 Spacer(Modifier.height(12.dp));Text("PATTERN · OPTIONAL",fontSize=11.sp,fontWeight=FontWeight.Bold,color=Muted);Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("Plain","Striped","Printed").forEach{p->FilterChip(selected=pattern==p,onClick={pattern=p},label={Text(p)})}}
 Spacer(Modifier.height(14.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){TextButton(onClick=dismiss,modifier=Modifier.weight(1f).height(48.dp)){Text("Cancel",color=Ink)};Button(onClick={chosen?.let{begin(name,it,pattern)}},enabled=name.isNotBlank()&&chosen!=null,modifier=Modifier.weight(1.5f).height(48.dp)){Text("Start Play · 30 min")}}
 }}
}
private fun hexColor(h:String)=try{Color(AColor.parseColor(h))}catch(_:Exception){Gold}
private fun clock(t:Long)=SimpleDateFormat("h:mm a",Locale.getDefault()).format(Date(t)).lowercase(Locale.getDefault())
private fun date(t:Long)=SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(Date(t))
private fun duration(ms:Long):String{val sec=ms.coerceAtLeast(0)/1000;return "%02d:%02d".format(sec/60,sec%60)}
